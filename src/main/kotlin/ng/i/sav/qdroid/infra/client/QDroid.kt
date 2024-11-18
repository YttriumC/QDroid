@file:OptIn(DelicateCoroutinesApi::class)

package ng.i.sav.qdroid.infra.client

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ng.i.sav.qdroid.infra.annotation.Order
import ng.i.sav.qdroid.infra.config.WsClient
import ng.i.sav.qdroid.infra.model.*
import ng.i.sav.qdroid.infra.util.Tools
import ng.i.sav.qdroid.log.Slf4kt
import org.springframework.web.socket.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import reactor.netty.http.client.HttpClient
import reactor.netty.http.websocket.WebsocketOutbound
import java.net.URI
import java.net.URL
import java.nio.ByteBuffer
import kotlin.concurrent.Volatile
import kotlin.system.exitProcess

/**
 *
 * @param intents [Intents]
 * */
class QDroid(
    private val appId: String,
    private val token: String,
    private val apiHost: URL,
    private val wsClient: WsClient,
    private val intents: Int,
    private val totalShards: Int,
    private val json: ObjectMapper,
    private val lifecycle: List<BotLifecycle>, //TODO
    private val eventDispatcher: BotEventDispatcher,
    private val currentShard: Int = 0,
    private val apiRequest: StatelessApiRequest,
) {

    @Volatile
    private var wsMsgSeq = 0

    @Volatile
    private lateinit var session: WebSocketSession
    private var wsURL: String = ""
    private var heartbeatTimeoutMillis = 40 * 1000L
    private var sessionId = ""
    private lateinit var user: User
    private lateinit var heartbeatJob: Job

    @Volatile
    private var _state = State.STANDBY

    var state
        get() = _state
        private set(value) {
            _state = value
            log.debug("QDroid state changed to: {}", _state)
        }

    val webSocketHandler: BotWebSocketHandler = BotWebSocketHandler()

    init {
        if (Tools.anyBlank(appId, token)) {
            throw IllegalArgumentException("appId or appToken is empty")
        }
        if (totalShards <= currentShard) {
            throw IllegalArgumentException("wrong shard num: $currentShard")
        }
        Order.sort(lifecycle)
    }

    enum class State(val state: String) {
        STANDBY("standby"),
        HELLO("hello"),
        AUTHENTICATION_SENT("authentication_sent"),
        CONNECTED("connected"),
        RESUME("resume"),
        SHUTDOWN("closed")
    }

    /**
     * 启动bot
     * */
    fun start() {
        QDroidScope.launch {
            val gatewayBot = apiRequest.getGatewayBot()
            this@QDroid.wsURL = gatewayBot.url
            lock.withLock {
                log.info("get websocket url: {}", gatewayBot.url)
                log.debug("recommended shards: {}", gatewayBot.shards)
                HttpClient.create().websocket().uri(wsURL).handle { inbound, outbound ->
                     outbound.sendString()
                    outbound.sendObject(Flux.create<TextMessage> {sink->
                        inbound.receive().asString().flatMap { Mono.just(TextMessage(it)) }
                            .doOnNext { handleMessage(sink, it) }.subscribeOn(Schedulers.immediate()).subscribe()
                    }.flatMap { msg-> }).neverComplete()
//TODO
                }.subscribe()

                wsClient.startConnection(this@QDroid, URI(wsURL))
                lifecycle.forEach {
                    runCatching { it.onStart(this@QDroid) }.exceptionOrNull()
                        ?.let { log.warn("Lifecycle onStart failed", it) }
                }
                delay((6_000 / gatewayBot.sessionStartLimit.maxConcurrency).toLong())
            }
        }
    }

    fun shutdown() {
        log.info("bot start shutting down")
        state = State.SHUTDOWN
        lifecycle.forEach {
            runCatching { it.onShutdown(this@QDroid) }.exceptionOrNull()?.let { log.warn("Bot difecycle shutdown") }
        }
        webSocketHandler.close()
    }

    /**
     * All messages' entry
     * */
    private fun handleMessage(session: WebsocketOutbound, message: WebSocketMessage<*>) {
        when (state) {
            State.HELLO -> helloStage(session, message)
            State.AUTHENTICATION_SENT -> authenticationStage(session, message)
            State.CONNECTED -> when (message) {
                is TextMessage -> handleTextMsg(session, message)
                is BinaryMessage -> handleBinMsg(session, message)
            }

            State.RESUME -> resumeStage(session, message)
            State.STANDBY, State.SHUTDOWN -> {
            }/*else -> {
                // should have done nothing
            }*/
        }

    }

    private fun helloStage(session: WebsocketOutbound, message: WebSocketMessage<*>) {
        message.toObj<Op10Hello>().let { payload ->
            payload.d?.let { heartbeatTimeoutMillis = it.heartbeatInterval.toLong() }
            payload.s?.let { wsMsgSeq = it.coerceAtLeast(wsMsgSeq) }
            session.startHeartbeat()
            session.sendText(
                OpCode.IDENTIFY, Authentication(getAuthToken(), intents, arrayOf(currentShard, totalShards))
            )
            state = State.AUTHENTICATION_SENT
        }
    }

    private fun WebsocketOutbound.startHeartbeat() {
        stopHeartbeat("New connection established")
        heartbeatJob = GlobalScope.launch {
            while (true) {
                delay((heartbeatTimeoutMillis - 10_000).coerceAtLeast(10_000))
                sendText(OpCode.HEARTBEAT)
            }
        }
    }

    private fun stopHeartbeat(msg: String) {
        if (this::heartbeatJob.isInitialized) {
            if (heartbeatJob.isActive) {
                heartbeatJob.cancel(msg)
            }
        }
    }

    private fun authenticationStage(session: WebSocketSession, message: WebSocketMessage<*>) {
        val msg = message.payload.toString()
        json.readValue(msg, Payload::class.java).let { payload ->
            when (payload.op) {
                OpCode.DISPATCH -> {
                    payload.s?.let { wsMsgSeq = it.coerceAtLeast(wsMsgSeq) }
                    payload.t?.let {
                        if (it == Event.READY) {
                            val readyEvent = payload.d!! as ReadyEvent
                            user = readyEvent.user
                            sessionId = readyEvent.sessionId
                            log.info(
                                "Bot connected: session id: {}, username: {}, shards: {}",
                                sessionId,
                                user.username,
                                readyEvent.shard
                            )
                        }
                    }
                    state = State.CONNECTED
                    log.debug("state changed to: {}", State.CONNECTED)
                    lifecycle.forEach {
                        runCatching { it.onAuthenticationSuccess(this) }.exceptionOrNull()
                            ?.let { log.warn("Lifecycle onAuthenticationSuccess failed", it) }
                    }
                }

                OpCode.INVALID_SESSION -> {
                    state = State.HELLO
                    throw RuntimeException("Authentication failed: ${getAuthToken()}")
                }

                OpCode.HEARTBEAT -> {
                    log.debug("Received heartbeat.")
                }

                else -> {
                    //nothing to do
                }
            }
        }
    }

    private fun resumeStage(session: WebSocketSession, message: WebSocketMessage<*>) {
        message.toObj<Any>().let { payload ->
            when (payload.op) {
                OpCode.DISPATCH -> {
                    payload.s?.let { wsMsgSeq = it.coerceAtLeast(wsMsgSeq) }
                    payload.t?.let {
                        if (it == Event.READY) {
                            val readyEvent = payload.d as ReadyEvent
                            user = readyEvent.user
                            sessionId = readyEvent.sessionId
                            log.info("Bot connected: session id: {}, user: {}", sessionId, user)
                        }
                    }
                    state = State.CONNECTED
                    session.startHeartbeat()
                }

                OpCode.RECONNECT, OpCode.INVALID_SESSION -> {
                    state = State.STANDBY
                }

                OpCode.HELLO -> session.sendText(OpCode.RESUME, Op6Resume(getAuthToken(), sessionId, wsMsgSeq))
                else -> {}
            }
        }
    }

    private fun handleTextMsg(session: WebSocketSession, msg: TextMessage) {
        val payload = json.readValue(msg.payload, Payload::class.java)
        when (payload.op) {
            OpCode.DISPATCH -> {
                try {
                    eventDispatcher.onEvent(apiRequest, payload, msg.payload)
                } catch (e: Exception) {
                    log.error("Handle message error", e)
                }
            }

            OpCode.RECONNECT -> {
                state = State.RESUME
                stopHeartbeat("Resume")
                log.info("Received RECONNECT: {}", msg.payload)
            }

            OpCode.HEARTBEAT_ACK -> {
                log.debug("Received HEARTBEAT_ACK: {}", msg.payload)
            }

            OpCode.HTTP_CALLBACK_ACK -> TODO()
            else -> {
                log.info("Received msg: {}", msg.payload)
            }
        }

    }

    private fun handleBinMsg(session: WebSocketSession, msg: BinaryMessage) {
        log.info("Binary mag: {}", msg)
    }

    inner class BotWebSocketHandler : WebSocketHandler {
        private var msgList = ArrayList<WebSocketMessage<*>>()
        private lateinit var session: WebSocketSession
        override fun afterConnectionEstablished(session: WebSocketSession) {
            if (state != State.RESUME) state =
                State.HELLO
            this.session = session
            this@QDroid.session = session
            log.info("Websocket Connection {} is established", session.uri)
        }

        override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
            if (!message.isLast) {
                msgList.add(message)
                return
            }
            when (message) {
                // ignore ping/pong message
                is PingMessage, is PongMessage -> return
                is TextMessage -> {
                    msgList.add(message)
                    StringBuilder(msgList.sumOf { it.payloadLength }).run {
                        msgList.forEach {
                            append(it.payload)
                        }
                        log.debug("Websocket received TextMessage: {}", this)
                        this@QDroid.handleMessage(session, TextMessage(this))
                        msgList.clear()
                    }
                }

                is BinaryMessage -> {
                    ByteBuffer.allocate(msgList.sumOf { it.payloadLength }).run {
                        msgList.forEach {
                            it as BinaryMessage
                            put(it.payload)
                        }
                        this@QDroid.handleMessage(session, BinaryMessage(this.asReadOnlyBuffer()))
                        msgList.clear()
                    }
                }
            }
        }

        override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
            state = State.RESUME
            if (heartbeatJob.isActive) heartbeatJob.cancel("Connection abort", exception)
            if (msgList.isNotEmpty()) {
                log.error(
                    "Last {} message(s) not consumed: {}",
                    msgList.size,
                    msgList.joinToString { it.payload.toString() },
                    exception
                )
            }
            throw exception
        }

        override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
            log.info("WS Connection '{}' is closed : {}", session.uri, closeStatus.code)
            if (state == State.SHUTDOWN) return
            stopHeartbeat("Connection closed: $closeStatus")
            when (closeStatus.code) {
                4001 -> log.error("无效的 opcode")
                4002 -> log.error("无效的 payload")
                4006 -> {
                    log.error("无效的 session id，无法继续 resume，请 identify")
                    state = State.STANDBY
                }

                4007 -> {
                    log.error("seq 错误")
                    state = State.STANDBY
                }

                4008 -> {
                    log.warn("发送 payload 过快，请重新连接，并遵守连接后返回的频控信息")
                    state = State.RESUME
                    wsClient.startConnection(this@QDroid, URI(wsURL))
                }

                4009 -> {
                    log.warn("连接过期，请重连并执行 resume 进行重新连接")
                    state = State.RESUME
                    wsClient.startConnection(this@QDroid, URI(wsURL))
                }

                4010 -> {
                    log.error("无效的 shard")
                    exitProcess(0)
                }

                4011 -> {
                    log.error("连接需要处理的 guild 过多，请进行合理的分片")
                    exitProcess(0)
                }

                4012 -> {
                    log.error("无效的 version")
                    exitProcess(0)
                }

                4013 -> {
                    log.error("无效的 intent")
                    exitProcess(0)
                }

                4014 -> {
                    log.error("intent 无权限")
                    exitProcess(0)
                }

                4914 -> {
                    log.error("机器人已下架,只允许连接沙箱环境,检验当前连接环境")
                    exitProcess(0)
                }

                4915 -> {
                    log.error("机器人已封禁,不允许连接,申请解封后再连接")
                    exitProcess(0)
                }

                else -> {
                    log.warn("内部错误，正在重连")
                    state = State.STANDBY
                    start()
                }
            }
        }

        override fun supportsPartialMessages(): Boolean = true

        fun close() {
            state = State.SHUTDOWN
            stopHeartbeat("Bot shutdown")
            if (session.isOpen) session.close(CloseStatus.NORMAL)
            lifecycle.forEach {
                try {
                    it.onShutdown(this@QDroid)
                } catch (e: Exception) {
                    TODO("Not yet implemented")
                }
            }
        }
    }


    private inline fun <reified T> Any.toObj(): Payload<T> {
        return when (this) {
            is CharSequence -> json.readValue(this.toString(), object : TypeReference<Payload<T>>() {})
            is TextMessage -> json.readValue(this.payload, object : TypeReference<Payload<T>>() {})
            else -> json.convertValue(this, object : TypeReference<Payload<T>>() {})
        }
    }

    private fun WebSocketSession.sendText(opCode: OpCode, d: Any? = null, t: Event? = null) {
        this.sendMessage(TextMessage(json.writeValueAsString(Payload.with(opCode, d, wsMsgSeq, t))))
    }

    private fun getAuthToken(): String = "Bot $appId.$token"

    override fun toString(): String {
        return "QDroid(state=$state, apiHost='$apiHost', appId='$appId', intents=$intents, currentShard=$currentShard, user=$user)"
    }

    companion object {
        private val log = Slf4kt.getLogger(QDroid::class.java)
        private val lock = Mutex()
    }

}

