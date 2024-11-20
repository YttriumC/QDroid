package ng.i.sav.qdroid.infra.client

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ng.i.sav.qdroid.infra.annotation.Order
import ng.i.sav.qdroid.infra.model.*
import ng.i.sav.qdroid.infra.util.Tools
import ng.i.sav.qdroid.log.Slf4kt
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketMessage
import reactor.core.Disposable
import reactor.core.publisher.Flux
import reactor.core.publisher.FluxSink
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import reactor.netty.http.client.HttpClient
import java.net.URL
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
    private lateinit var wsClientDisposable: Disposable
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
            log.info("QDroid state changed to: {}", _state)
        }

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
                wsClientDisposable = HttpClient.create().websocket().uri(wsURL).handle { inbound, outbound ->
                    if (state != State.RESUME) state =
                        State.HELLO
                    inbound.receiveCloseStatus().doOnNext {
                        onReceiveCloseStatus(it.code(), it.reasonText())
                    }.subscribeOn(Schedulers.immediate()).subscribe()
                    Flux.create { sink ->
                        inbound.aggregateFrames().receiveFrames().flatMap {
                            when (it) {
                                is TextWebSocketFrame -> {
                                    Mono.just(it.text())
                                }

                                else -> {
                                    Mono.empty()
                                }
                            }
                        }.doOnNext {
                            if (it != null)
                                handleMessage(sink, it)
                        }.subscribeOn(Schedulers.immediate()).subscribe()
                    }.flatMap {
                        when (it) {
                            is TextMessage -> {
                                outbound.sendString(Mono.just(it.payload))
                            }

                            else -> {
                                outbound.sendObject(it.payload)
                            }
                        }
                    }.subscribeOn(Schedulers.immediate()).subscribe()
                    outbound.neverComplete()
                }.subscribeOn(Schedulers.immediate()).subscribe()

                lifecycle.forEach {
                    runCatching { it.onStart(this@QDroid) }.exceptionOrNull()
                        ?.let { log.warn("Lifecycle onStart failed", it) }
                }
                delay((6_000 / gatewayBot.sessionStartLimit.maxConcurrency).toLong())
            }
            log.info("QDroid instance started")
        }
    }


    fun shutdown() {
        log.info("bot start shutting down")
        state = State.SHUTDOWN
        lifecycle.forEach {
            runCatching { it.onShutdown(this@QDroid) }.exceptionOrNull()?.let { log.warn("Bot lifecycle shutdown") }
        }
        if (::wsClientDisposable.isInitialized) wsClientDisposable.dispose()
    }

    /**
     * All messages' entry
     * */
    private fun handleMessage(sink: FluxSink<WebSocketMessage<*>>, message: String) {
        when (state) {
            State.HELLO -> helloStage(sink, message)
            State.AUTHENTICATION_SENT -> authenticationStage(sink, message)
            State.CONNECTED -> handleTextMsg(sink, message)

            State.RESUME -> resumeStage(sink, message)
            State.STANDBY, State.SHUTDOWN -> {
            }/*else -> {
                // should have done nothing
            }*/
        }

    }

    private fun helloStage(sink: FluxSink<WebSocketMessage<*>>, message: String) {
        json.readValue(message, object : TypeReference<Payload<Op10Hello>>() {}).let { payload ->
            payload.d?.let { heartbeatTimeoutMillis = it.heartbeatInterval.toLong() }
            payload.s?.let { wsMsgSeq = it.coerceAtLeast(wsMsgSeq) }
            sink.sendText(
                OpCode.IDENTIFY, Authentication(getAuthToken(), intents, arrayOf(currentShard, totalShards))
            )
            state = State.AUTHENTICATION_SENT
        }
    }

    private fun FluxSink<WebSocketMessage<*>>.startHeartbeat() {
        stopHeartbeat("New connection established")
        heartbeatJob = QDroidScope.launch {
            while (isCancelled.not()) {
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

    private fun authenticationStage(sink: FluxSink<WebSocketMessage<*>>, message: String) {
        json.readValue(message, Payload::class.java).let { payload ->
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
                    sink.startHeartbeat()
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

    private fun resumeStage(sink: FluxSink<WebSocketMessage<*>>, message: String) {
        json.readValue(message, object : TypeReference<Payload<*>>() {}).let { payload ->
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
                    sink.startHeartbeat()
                }

                OpCode.RECONNECT, OpCode.INVALID_SESSION -> {
                    state = State.STANDBY
                }

                OpCode.HELLO -> sink.sendText(OpCode.RESUME, Op6Resume(getAuthToken(), sessionId, wsMsgSeq))
                else -> {}
            }
        }
    }

    private fun handleTextMsg(sink: FluxSink<WebSocketMessage<*>>, message: String) {
        val payload = json.readValue(message, Payload::class.java)
        when (payload.op) {
            OpCode.DISPATCH -> {
                try {
                    eventDispatcher.onEvent(apiRequest, payload, message)
                } catch (e: Exception) {
                    log.error("Handle message error", e)
                }
            }

            OpCode.RECONNECT -> {
                state = State.RESUME
                stopHeartbeat("Resume")
                log.info("Received RECONNECT: {}", message)
            }

            OpCode.HEARTBEAT_ACK -> {
                log.debug("Received HEARTBEAT_ACK: {}", message)
            }
            OpCode.HTTP_CALLBACK_ACK -> TODO()
            else -> {
                log.info("Received msg: {}", message)
            }
        }

    }

    private fun handleBinMsg(sink: FluxSink<WebSocketMessage<*>>, message: String) {
        log.info("Binary mag: {}", message)
    }

    private fun onReceiveCloseStatus(closeStatus: Int, reason: String) {
        log.info("WS Connection is closed : {}, reason: {}", closeStatus, reason)
        if (state == State.SHUTDOWN) return
        stopHeartbeat("Connection closed: $closeStatus")
        when (closeStatus) {
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
                start()
            }

            4009 -> {
                log.warn("连接过期，请重连并执行 resume 进行重新连接")
                state = State.RESUME
                start()
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


    private fun FluxSink<WebSocketMessage<*>>.sendText(opCode: OpCode, d: Any? = null, t: Event? = null) {
        this.next(TextMessage(json.writeValueAsString(Payload.with(opCode, d, wsMsgSeq, t))))
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

