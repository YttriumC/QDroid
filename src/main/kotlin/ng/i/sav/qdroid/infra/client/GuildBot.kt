@file:OptIn(DelicateCoroutinesApi::class)

package ng.i.sav.qdroid.infra.client

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import ng.i.sav.qdroid.infra.annotation.Order
import ng.i.sav.qdroid.infra.config.RestClient
import ng.i.sav.qdroid.infra.config.WsClient
import ng.i.sav.qdroid.infra.model.*
import ng.i.sav.qdroid.infra.util.Tools
import ng.i.sav.qdroid.infra.util.toObj
import ng.i.sav.qdroid.log.Slf4kt
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.web.socket.*
import org.springframework.web.util.DefaultUriBuilderFactory
import org.springframework.web.util.DefaultUriBuilderFactory.EncodingMode
import org.springframework.web.util.UriComponentsBuilder
import java.io.File
import java.net.URI
import java.net.URL
import java.nio.ByteBuffer
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.concurrent.Volatile
import kotlin.system.exitProcess

/**
 *
 * @param intents [Intents]
 * */
class GuildBot(
    private val appId: String,
    private val token: String,
    private val apiHost: URL,
    private val restClient: RestClient,
    private val wsClient: WsClient,
    private val httpRequestPool: HttpRequestPool,
    private val intents: Int,
    private val totalShards: Int,
    private val json: ObjectMapper,
    private val lifecycle: List<BotLifecycle>,
    private val eventDispatcher: BotEventDispatcher,
    private val currentShard: Int = 0
) : BotApi {

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

    val id get() = user.id
    val username get() = user.username
    val bot get() = user.bot
    val state get() = _state
    val startTime: LocalDateTime get() = if (this::_startTime.isInitialized) _startTime else LocalDateTime.MIN
    private lateinit var _startTime: LocalDateTime

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
        STANDBY("standby"), HELLO("hello"), AUTHENTICATION_SENT("authentication_sent"), CONNECTED("connected"), RESUME("resume"), SHUTDOWN(
            "closed"
        )
    }

    /**
     * 启动bot
     * */
    fun start() {
        _startTime = LocalDateTime.now()
        val gatewayBot = getGatewayBot()
        this.wsURL = gatewayBot.url
        GlobalScope.launch {
            lock.withLock {
                log.info("get websocket url: {}", gatewayBot.url)
                log.debug("recommended shards: {}", gatewayBot.shards)
                wsClient.startConnection(this@GuildBot, URI(wsURL))
                lifecycle.forEach {
                    runCatching { it.onStart(this@GuildBot) }.exceptionOrNull()
                        ?.let { log.warn("Lifecycle onStart failed", it) }
                }
                delay((6_000 / gatewayBot.sessionStartLimit.maxConcurrency).toLong())
            }
        }
    }

    fun shutdown() {
        log.info("bot start shutting down")
        _state = State.SHUTDOWN
        lifecycle.forEach {
            runCatching { it.onShutdown(this@GuildBot) }.exceptionOrNull()?.let { log.warn("Bot difecycle shutdown") }
        }
        webSocketHandler.close()
    }

    private fun throwAtLeastOne(): Nothing {
        throw IllegalArgumentException("At least one arg needed.")
    }

    override fun getUsersMe(): User {
        return get(ApiPath.GET_USERS_ME)
    }

    override fun getUsersMeGuilds(before: String?, after: String?, limit: Int): ArrayList<Guild> {
        if (Tools.allBlank(before, after)) {
            throwAtLeastOne()
        }
        val map = hashMapOf<String, Any>()
        val l = if (limit < 1 || limit > 100) 100 else limit
        map.putNotNull("before", before).putNotNull("after", after).putNotNull("limit", l)
        return get(ApiPath.GET_USERS_ME_GUILDS, params = map)
    }

    override fun getGuilds(guildId: String): Guild {
        return get(ApiPath.GET_GUILDS, guildId)
    }

    override fun getGuildsChannels(guildId: String): List<Channel> {
        return get(ApiPath.GET_GUILDS_CHANNELS, guildId)
    }

    override fun getChannels(channelId: String): ChannelDetail {
        return get(ApiPath.GET_CHANNELS, channelId)
    }

    override fun createGuildsChannels(guildId: String, channel: CreateChannel): Channel {
        return post(ApiPath.CREATE_GUILDS_CHANNELS, channel, uriVariables = arrayOf(guildId))
    }

    override fun modifyChannels(channelId: String, modifyChannel: ModifyChannel): Channel {
        return patch(ApiPath.MODIFY_CHANNELS, modifyChannel, channelId)
    }

    override fun deleteChannels(channelId: String) {
        return delete(ApiPath.DELETE_CHANNELS, channelId, body = Unit)
    }

    override fun getChannelsOnlineNums(channelId: String): OnlineNums {
        return get(ApiPath.GET_CHANNELS_ONLINE_NUMS, uriVariables = arrayOf(channelId))
    }

    override fun getGuildsMembers(guildId: String, after: String, limit: Int): List<Member> {
        val l = if (limit < 1 || limit > 400) 1 else limit
        val params = hashMapOf<String, Any>("after" to after, "limit" to l)
        return get(ApiPath.GET_GUILDS_MEMBERS, guildId, params = params)
    }

    override fun getGuildsMember(guildId: String, userId: String): Member {
        return get(ApiPath.GET_GUILDS_MEMBER, guildId, userId)
    }

    override fun getGuildsMembersRoles(guildId: String, roleId: String, startIndex: String, limit: Int): RolesResp {
        val l = if (limit < 1 || limit > 400) 1 else limit
        val params = hashMapOf<String, Any>("start_index" to startIndex, "limit" to l)
        return get(ApiPath.GET_GUILDS_MEMBERS_ROLES, guildId, roleId, params = params)
    }

    override fun deleteGuildsMembers(
        guildId: String, userId: String, addBlacklist: Boolean, deleteHistoryMsgDays: Int
    ) {
        val d = if (arrayOf(-1, 0, 3, 7, 15, 30).contains(deleteHistoryMsgDays)) deleteHistoryMsgDays else 0
        hashMapOf("add_blacklist" to addBlacklist, "delete_history_msg_days" to d)
        return delete(ApiPath.DELETE_GUILDS_MEMBERS, guildId, userId, body = Unit)
    }

    override fun getGuildsRoles(guildId: String): RolesListResp {
        return get(ApiPath.GET_GUILDS_ROLES, guildId)
    }

    override fun createGuildsRoles(guildId: String, name: String?, color: UInt?, hoist: Int?): CreatedRole {
        val b = hashMapOf<String, Any>().putNotNull("name", name).putNotNull("color", color).putNotNull("hoist", hoist)
        return post(ApiPath.CREATE_GUILDS_ROLES, b, guildId)
    }

    override fun modifyGuildsRoles(
        guildId: String, roleId: String, name: String?, color: UInt?, hoist: Int?
    ): ChangedRole {
        val b = hashMapOf<String, Any>().putNotNull("name", name).putNotNull("color", color).putNotNull("hoist", hoist)
        return patch(ApiPath.MODIFY_GUILDS_ROLES, b, guildId, roleId)
    }

    override fun deleteGuildsRoles(guildId: String, roleId: String) {
        return delete(ApiPath.DELETE_GUILDS_ROLES, guildId, roleId, body = Unit)
    }

    override fun addGuildsRolesMembers(guildId: String, userId: String, roleId: String, channel: Channel) {
        return put(ApiPath.ADD_GUILDS_ROLES_MEMBERS, channel, guildId, userId, roleId)
    }

    override fun deleteGuildsRolesMembers(guildId: String, roleId: String, userId: String, channel: Channel) {
        return delete(ApiPath.DELETE_GUILDS_ROLES_MEMBERS, guildId, roleId, userId, body = channel)
    }

    override fun getChannelsMembersPermissions(channelId: String, userId: String): ChannelPermissions {
        return get(ApiPath.GET_CHANNELS_MEMBERS_PERMISSIONS, channelId, userId)
    }

    @Suppress("DUPLICATES")
    override fun modifyChannelsMembersPermissions(
        channelId: String,
        userId: String,
        add: UInt?,
        remove: UInt?
    ) {
        if (Tools.allNull(add, remove)) {
            throwAtLeastOne()
        }
        val range = UIntRange("1".toUInt(), "15".toUInt())
        val a = add?.let {
            if (range.contains(it)) it
            else "1".toUInt()
        }
        val r = remove?.let {
            if (range.contains(it)) it
            else "1".toUInt()
        }
        val b = hashMapOf<String, Any>()
        b.putNotNull("add", a)
        b.putNotNull("remove", r)
        return put(ApiPath.MODIFY_CHANNELS_MEMBERS_PERMISSIONS, b, channelId, userId)
    }

    override fun getChannelsRolesPermissions(channelId: String, roleId: String): ChannelPermissions {
        return get(ApiPath.GET_CHANNELS_ROLES_PERMISSIONS, channelId, roleId)
    }

    @Suppress("DUPLICATES")// anti-duplicate
    override fun modifyChannelsRolesPermissions(
        channelId: String,
        roleId: String,
        add: UInt?,
        remove: UInt?
    ): ChannelPermissions {
        if (Tools.allNull(add, remove)) {
            throwAtLeastOne()

        }
        val range = UIntRange("1".toUInt(), "15".toUInt())
        val r = remove?.let {
            if (range.contains(it)) it
            else "1".toUInt()
        }
        val a = add?.let {
            if (range.contains(it)) it
            else "1".toUInt()
        }
        val b = hashMapOf<String, Any>()
        b.putNotNull("add", a)
        b.putNotNull("remove", r)
        return put(ApiPath.MODIFY_CHANNELS_ROLES_PERMISSIONS, b, channelId, roleId)
    }

    override fun getChannelsMessages(channelId: String, messageId: String): Message {
        return get(ApiPath.GET_CHANNELS_MESSAGES, channelId, messageId)
    }

    @Suppress("DUPLICATES")
    override fun postChannelsMessages(
        channelId: String,
        content: String?,
        embed: MessageEmbed?,
        ark: MessageArk?,
        messageReference: MessageReference?,
        image: String?,
        msgId: String?,
        eventId: String?,
        markdown: MessageMarkdown?,
        keyboard: MessageKeyboard?,
        fileImage: File?
    ): Message {
        if (Tools.allNull(content, embed, ark, image, fileImage, markdown)) {
            throwAtLeastOne()
        }
        val body = HashMap<String, Any>()
        body.putNotNull("content", content)
        body.putNotNull("embed", embed)
        body.putNotNull("message_reference", messageReference)
        body.putNotNull("image", image)
        body.putNotNull("msg_id", msgId)
        body.putNotNull("event_id", eventId)
        body.putNotNull("markdown", markdown)
        if (markdown != null) body.putNotNull("keyboard", keyboard)
        return if (fileImage == null) {
            post(ApiPath.POST_CHANNELS_MESSAGES, body, channelId)
        } else {
            body.putNotNull("file_image", fileImage.inputStream())
            val headers = HttpHeaders()
            headers.contentType = MediaType.MULTIPART_FORM_DATA
            post(ApiPath.POST_CHANNELS_MESSAGES, body, headers = headers, uriVariables = arrayOf(channelId))
        }
    }

    override fun deleteChannelsMessages(channelId: String, messageId: String, hideTip: Boolean) {
        return delete(
            ApiPath.DELETE_CHANNELS_MESSAGES,
            channelId,
            messageId,
            body = Unit,
            params = hashMapOf("hidetip" to hideTip)
        )
    }

    override fun getGuildsMessageSetting(guildId: String): MessageSetting {
        return get(ApiPath.GET_GUILDS_MESSAGE_SETTING, guildId)
    }

    override fun createUsersDms(recipientId: String, sourceGuildId: String): DirectMessage {
        val b = hashMapOf("recipient_id" to recipientId, "source_guild_id" to sourceGuildId)
        return post(ApiPath.CREATE_USERS_DMS, body = b)
    }

    @Suppress("DUPLICATES")
    override fun postDmsMessages(
        guildId: String,
        content: String?,
        embed: MessageEmbed?,
        ark: MessageArk?,
        messageReference: MessageReference?,
        image: String?,
        msgId: String?,
        eventId: String?,
        markdown: MessageMarkdown?,
        keyboard: MessageKeyboard?,
        fileImage: File?
    ): Message {
        if (Tools.allNull(content, embed, ark, image, fileImage, markdown)) {
            throwAtLeastOne()
        }
        val body = HashMap<String, Any>()
        body.putNotNull("content", content)
        body.putNotNull("embed", embed)
        body.putNotNull("message_reference", messageReference)
        body.putNotNull("image", image)
        body.putNotNull("msg_id", msgId)
        body.putNotNull("event_id", eventId)
        body.putNotNull("markdown", markdown)
        if (markdown != null) body.putNotNull("keyboard", keyboard)
        return if (fileImage == null) {
            post(ApiPath.POST_CHANNELS_MESSAGES, body, guildId)
        } else {
            body.putNotNull("file_image", fileImage.inputStream())
            val headers = HttpHeaders()
            headers.contentType = MediaType.MULTIPART_FORM_DATA
            post(ApiPath.POST_DMS_MESSAGES, body, guildId, headers = headers)
        }
    }

    override fun deleteDmsMessages(guildId: String, messageId: String, hideTip: Boolean) {
        return delete(
            ApiPath.DELETE_DMS_MESSAGES,
            guildId, messageId,
            body = Unit,
            params = hashMapOf("hidetip" to hideTip)
        )
    }

    override fun setGuildsMute(guildId: String, muteEndTimestamp: LocalDateTime?, muteSeconds: Duration) {
        val b = hashMapOf<String, Any>()
        b.putNotNull("mute_end_timestamp", muteEndTimestamp?.toEpochSecond(ZoneOffset.UTC))
        b.putNotNull("mute_seconds", muteSeconds.seconds)
        return patch(ApiPath.SET_GUILDS_MUTE, b, guildId)
    }

    override fun setGuildsMuteList(
        guildId: String,
        userIds: List<String>,
        muteEndTimestamp: LocalDateTime?,
        muteSeconds: Duration
    ): List<String> {
        val b = hashMapOf<String, Any>()
        b.putNotNull("mute_end_timestamp", muteEndTimestamp?.toEpochSecond(ZoneOffset.UTC))
        b.putNotNull("mute_seconds", muteSeconds.seconds)
        b.putNotNull("user_ids", userIds)
        return patch(ApiPath.SET_GUILDS_MUTE_LIST, b, guildId)
    }

    override fun setGuildsMembersMute(
        guildId: String,
        userId: String,
        muteEndTimestamp: LocalDateTime?,
        muteSeconds: Duration
    ) {
        val b = hashMapOf<String, Any>()
        b.putNotNull("mute_end_timestamp", muteEndTimestamp?.toEpochSecond(ZoneOffset.UTC))
        b.putNotNull("mute_seconds", muteSeconds.seconds)
        return patch(ApiPath.SET_GUILDS_MEMBERS_MUTE, b, guildId, userId)
    }

    override fun createGuildsAnnounces(
        guildId: String,
        channelId: String?,
        messageId: String?,
        announcesType: Int,
        recommendChannels: List<RecommendChannel>?
    ): Announces {
        val b = hashMapOf<String, Any>()
        b.putNotNull("message_id", messageId)
        b.putNotNull("channel_id", channelId)
        b.putNotNull("announces_type", announcesType)
        b.putNotNull("recommend_channels", recommendChannels)
        return post(ApiPath.CREATE_GUILDS_ANNOUNCES, b, guildId)
    }

    override fun deleteGuildsAnnounces(guildId: String, messageId: String) {
        return delete(ApiPath.DELETE_GUILDS_ANNOUNCES, guildId, messageId, body = Unit)
    }

    override fun addChannelsPins(channelId: String, messageId: String): PinsMessage {
        return put(ApiPath.ADD_CHANNELS_PINS, body = Unit, channelId, messageId)
    }

    override fun deleteChannelsPins(channelId: String, messageId: String) {
        return delete(ApiPath.DELETE_CHANNELS_PINS, channelId, messageId, body = Unit)
    }

    override fun getChannelsPins(channelId: String): PinsMessage {
        return get(ApiPath.GET_CHANNELS_PINS, channelId)
    }

    override fun getChannelsSchedules(channelId: String, since: LocalDateTime?): List<Schedule> {
        val p = hashMapOf<String, Any>()
        p.putNotNull("since", since?.toEpochSecond(ZoneOffset.UTC)?.times(1000))
        return get(ApiPath.GET_CHANNELS_SCHEDULES, channelId, params = p)
    }

    override fun getChannelsSchedule(channelId: String, scheduleId: String): Schedule {
        return get(ApiPath.GET_CHANNELS_SCHEDULE, channelId, scheduleId)
    }

    override fun createChannelsSchedules(channelId: String, schedule: Schedule): Schedule {
        val b = hashMapOf("schedule" to schedule)
        return post(ApiPath.CREATE_CHANNELS_SCHEDULES, b, channelId)
    }

    override fun modifyChannelsSchedules(channelId: String, scheduleId: String, schedule: Schedule) {
        val b = hashMapOf("schedule" to schedule)
        return patch(ApiPath.MODIFY_CHANNELS_SCHEDULES, b, channelId, scheduleId)
    }

    override fun deleteChannelsSchedules(channelId: String, scheduleId: String) {
        return delete(ApiPath.DELETE_CHANNELS_SCHEDULES, channelId, scheduleId, body = Unit)
    }

    override fun setChannelsMessagesReactions(channelId: String, messageId: String, emoji: Emoji) {
        return put(
            ApiPath.SET_CHANNELS_MESSAGES_REACTIONS,
            Unit,
            channelId,
            messageId,
            emoji.type.type.toString(),
            emoji.id
        )
    }

    override fun deleteChannelsMessagesReactions(channelId: String, messageId: String, emoji: Emoji) {
        return delete(
            ApiPath.DELETE_CHANNELS_MESSAGES_REACTIONS,
            channelId,
            messageId,
            emoji.type.type.toString(),
            emoji.id,
            body = Unit
        )
    }

    override fun getChannelsMessagesReactions(
        channelId: String, messageId: String, emoji: Emoji, cookie: String?, limit: Int
    ): ReactionList {
        val l = if (limit < 1 || limit > 50) 20 else limit
        val p = hashMapOf<String, Any>("limit" to l)
        p.putNotNull("cookie", cookie)
        return get(
            ApiPath.GET_CHANNELS_MESSAGES_REACTIONS,
            channelId,
            messageId,
            emoji.type.type.toString(),
            emoji.id,
            params = p
        )
    }

    override fun setChannelsAudio(channelId: String, audioControl: AudioControl) {
        post<AudioControl, Any>(ApiPath.SET_CHANNELS_AUDIO, audioControl, channelId)
    }

    override fun putChannelsMic(channelId: String) {
        put<Unit, Any>(ApiPath.PUT_CHANNELS_MIC, Unit, channelId)
    }

    override fun deleteChannelsMic(channelId: String) {
        delete<Unit, Any>(ApiPath.DELETE_CHANNELS_MIC, channelId, body = Unit)
    }

    override fun getChannelsThreads(channelId: String): ThreadsResp {
        return get(ApiPath.GET_CHANNELS_THREADS, channelId)
    }

    override fun getChannelsThread(channelId: String, threadId: String): ThreadResp {
        return get(ApiPath.GET_CHANNELS_THREAD, channelId, threadId)
    }

    override fun createChannelsThreads(
        channelId: String,
        title: String,
        content: String,
        format: Format
    ): CreateThreadResp {
        val b = hashMapOf<String, Any>("title" to title, "content" to content, "format" to format.value)
        return put(ApiPath.CREATE_CHANNELS_THREADS, b, channelId)
    }


    override fun deleteChannelsThreads(channelId: String, threadId: String) {
        return delete(ApiPath.DELETE_CHANNELS_THREADS, channelId, threadId, body = Unit)
    }

    override fun getGuildsApiPermission(guildId: String): List<APIPermission> {
        return get(ApiPath.GET_GUILDS_API_PERMISSION, guildId)
    }

    override fun createGuildsApiPermissionDemand(
        guildId: String, channelId: String, apiIdentify: APIPermissionDemandIdentify, desc: String
    ): APIPermissionDemand {
        val b = hashMapOf("channel_id" to channelId, "api_identify" to apiIdentify, "desc" to desc)
        return post(ApiPath.CREATE_GUILDS_API_PERMISSION_DEMAND, b, guildId)
    }

    override fun getGateway(): String {
        return httpRequestPool.runSync {
            get<Map<String, Any>>(ApiPath.GET_GATEWAY)["url"]?.toString() ?: throw BotRequestFailure.toPath(
                ApiPath.GET_GATEWAY
            )
        }

    }

    override fun getGatewayBot(): WebsocketApi {
        return httpRequestPool.runSync {
            get(ApiPath.GET_GATEWAY_BOT)
        }
    }

    private val defaultUriBuilderFactory = DefaultUriBuilderFactory().apply {
        encodingMode = EncodingMode.URI_COMPONENT
    }

    /**
     * All messages' entry
     * */
    private fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
        when (_state) {
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

    private fun helloStage(session: WebSocketSession, message: WebSocketMessage<*>) {
        message.toObj<Op10Hello>().let { payload ->
            payload.d?.let { heartbeatTimeoutMillis = it.heartbeatInterval.toLong() }
            payload.s?.let { wsMsgSeq = it.coerceAtLeast(wsMsgSeq) }
            session.startHeartbeat()
            session.sendText(
                OpCode.IDENTIFY, Authentication(getAuthToken(), intents, arrayOf(currentShard, totalShards))
            )
            _state = State.AUTHENTICATION_SENT
        }
    }

    private fun WebSocketSession.startHeartbeat() {
        stopHeartbeat("New connection established")
        heartbeatJob = GlobalScope.launch {
            // repeat()
            repeat(Int.MAX_VALUE) {
                delay((heartbeatTimeoutMillis - 10_000).coerceAtLeast(10_000))
                sendText(OpCode.HEARTBEAT)
            }/*while (true) {
            }*/
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
        msg.toObj<Any>().let { payload ->
            when (payload.op) {
                OpCode.DISPATCH -> {
                    payload.s?.let { wsMsgSeq = it.coerceAtLeast(wsMsgSeq) }
                    payload.t?.let {
                        if (it == "READY") {
                            val readyEvent = json.toObj<ReadyEvent>(payload.d!!)
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
                    _state = State.CONNECTED
                    log.debug("state changed to: {}", State.CONNECTED)
                    lifecycle.forEach {
                        runCatching { it.onAuthenticationSuccess(this) }.exceptionOrNull()
                            ?.let { log.warn("Lifecycle onAuthenticationSuccess failed", it) }
                    }
                }

                OpCode.INVALID_SESSION -> {
                    _state = State.HELLO
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
                        if (it == "READY") {
                            val readyEvent = json.toObj<ReadyEvent>(payload.d!!)
                            user = readyEvent.user
                            sessionId = readyEvent.sessionId
                            log.info("Bot connected: session id: {}, {}", sessionId, user)
                        }
                    }
                    _state = State.CONNECTED
                    session.startHeartbeat()
                }

                OpCode.RECONNECT, OpCode.INVALID_SESSION -> {
                    _state = State.STANDBY
                }

                OpCode.HELLO -> session.sendText(OpCode.RESUME, Op6Resume(getAuthToken(), sessionId, wsMsgSeq))
                else -> {}
            }
        }
    }

    private fun handleTextMsg(session: WebSocketSession, msg: TextMessage) {
        val payload = msg.toObj<Any>()
        when (payload.op) {
            OpCode.DISPATCH -> {
                try {
                    eventDispatcher.onEvent(this, msg.payload)
                } catch (e: Exception) {
                    log.error("Handle message error", e)
                }
            }

            OpCode.RECONNECT -> {
                _state = State.RESUME
                stopHeartbeat("Resume")
            }

            OpCode.HEARTBEAT_ACK -> {
                log.info("Received msg: {}", msg.payload)
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


    private val apiHostString = apiHost.toString()


    private inline fun <reified T> get(
        path: String, vararg uriVariables: String, params: Map<String, Any>? = null, headers: HttpHeaders? = null
    ): T {
        return noBody(HttpMethod.GET, path, params, object : ParameterizedTypeReference<T>() {}, headers, *uriVariables)
    }

    private inline fun <B, reified T> delete(
        path: String, vararg uriVariables: String,
        body: B, params: Map<String, Any>? = null, headers: HttpHeaders? = null
    ): T {
        return withBody(
            HttpMethod.DELETE, path, params, body, object : ParameterizedTypeReference<T>() {}, headers, *uriVariables
        )
    }

    private inline fun <B, reified T> post(
        path: String,
        body: B,
        vararg uriVariables: String,
        params: Map<String, Any>? = null,
        headers: HttpHeaders? = null
    ): T {
        return withBody(
            HttpMethod.POST, path, params, body, object : ParameterizedTypeReference<T>() {}, headers, *uriVariables
        )
    }

    private inline fun <B, reified T> patch(
        path: String,
        body: B,
        vararg uriVariables: String,
        params: Map<String, Any>? = null,
        headers: HttpHeaders? = null
    ): T {
        return withBody(
            HttpMethod.PATCH, path, params, body, object : ParameterizedTypeReference<T>() {}, headers, *uriVariables
        )
    }

    private inline fun <B, reified T> put(
        path: String,
        body: B,
        vararg uriVariables: String,
        params: Map<String, Any>? = null,
        headers: HttpHeaders? = null
    ): T {
        return withBody(
            HttpMethod.PUT, path, params, body, object : ParameterizedTypeReference<T>() {}, headers, *uriVariables
        )
    }

    private inline fun <reified T> noBody(
        method: HttpMethod,
        path: String,
        params: Map<String, Any>? = null,
        responseType: ParameterizedTypeReference<T>,
        headers: HttpHeaders? = null,
        vararg uriVariables: String
    ): T {
        log.debug("Start GET request: {}", path)
        val url = "${apiHostString.removeSuffix("/")}${if (path.startsWith('/')) "" else '/'}$path"
        val builder = UriComponentsBuilder.fromHttpUrl(url)
        params?.forEach { (k, v) ->
            builder.queryParam(k, v)
        }
        return restClient.exchange(
            builder.buildAndExpand(*uriVariables).toUri(), method, HttpEntity<Unit>(HttpHeaders().apply {
                if ((headers?.get(HttpHeaders.AUTHORIZATION)) == null) set(
                    HttpHeaders.AUTHORIZATION, "Bot $appId.$token"
                )
                if (headers != null) addAll(headers)
            }), responseType
        ).body.also {
            log.info("Send request: GET {}, response with: {}", path, it)
        } ?: throw BotRequestFailure("")
    }

    private inline fun <B, reified T> withBody(
        method: HttpMethod,
        path: String,
        params: Map<String, Any>? = null,
        body: B,
        responseType: ParameterizedTypeReference<T>,
        headers: HttpHeaders? = null,
        vararg uriVariables: String
    ): T {
        log.debug("Start POST request: {}, body: {}", path, body)
        val url = "${apiHostString.removeSuffix("/")}/${path.removePrefix("/")}"
        val builder = UriComponentsBuilder.fromHttpUrl(url)
        params?.forEach { (k, v) ->
            builder.queryParam(k, v)
        }
        return restClient.exchange(
            builder.buildAndExpand(*uriVariables).toUri(), method, HttpEntity(body, HttpHeaders().apply {
                if ((headers?.get(HttpHeaders.AUTHORIZATION)) == null) set(
                    HttpHeaders.AUTHORIZATION, "Bot $appId.$token"
                )
                if (headers != null) addAll(headers)
            }), responseType
        ).body.also {
            log.debug("Send request: POST {}, body: {}, response with: {}", path, body, it)
        } ?: throw BotRequestFailure("")
    }

    inner class BotWebSocketHandler : WebSocketHandler {
        private var msgList = ArrayList<WebSocketMessage<*>>()
        private lateinit var session: WebSocketSession
        override fun afterConnectionEstablished(session: WebSocketSession) {
            if (_state != State.RESUME) _state =
                State.HELLO
            this.session = session
            this@GuildBot.session = session
            log.info("WS Connection {} is established", session.uri)
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
                        this@GuildBot.handleMessage(session, TextMessage(this))
                        msgList.clear()
                    }
                }

                is BinaryMessage -> {
                    ByteBuffer.allocate(msgList.sumOf { it.payloadLength }).run {
                        msgList.forEach {
                            it as BinaryMessage
                            put(it.payload)
                        }
                        this@GuildBot.handleMessage(session, BinaryMessage(this.asReadOnlyBuffer()))
                        msgList.clear()
                    }
                }
            }
        }

        override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
            _state = State.RESUME
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
            if (_state == State.SHUTDOWN) return
            stopHeartbeat("Connection closed: $closeStatus")
            when (closeStatus.code) {
                4008 -> {
                    log.warn("发送 payload 过快，请重新连接，并遵守连接后返回的频控信息")
                    _state = State.RESUME
                    wsClient.startConnection(this@GuildBot, URI(wsURL))
                }

                4009 -> {
                    log.warn("连接过期，请重连并执行 resume 进行重新连接")
                    _state = State.RESUME
                    wsClient.startConnection(this@GuildBot, URI(wsURL))
                }

                4001 -> log.error("无效的 opcode")
                4002 -> log.error("无效的 payload")
                4006 -> {
                    log.error("无效的 session id，无法继续 resume，请 identify")
                    _state = State.STANDBY
                }

                4007 -> {
                    log.error("seq 错误")
                    _state = State.STANDBY
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
                    log.warn("内部错误，请重连")
                    _state = State.STANDBY
                    start()
                }
            }
        }

        override fun supportsPartialMessages(): Boolean = true

        fun close() {
            _state = State.SHUTDOWN
            stopHeartbeat("Bot shutdown")
            if (session.isOpen) session.close(CloseStatus.NORMAL)
            lifecycle.forEach {
                it.onShutdown(this@GuildBot)
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

    private fun WebSocketSession.sendText(opCode: OpCode, d: Any? = null, t: String? = null) {
        this.sendMessage(TextMessage(json.writeValueAsString(Payload.with(opCode, d, wsMsgSeq, t))))
    }

    private fun getAuthToken(): String = "Bot $appId.$token"

    private fun <K, V> MutableMap<K, V>.putNotNull(key: K, value: V?): MutableMap<K, V> {
        if (value != null) this[key] = value
        return this
    }

    companion object {
        private val log = Slf4kt.getLogger(GuildBot::class.java)
        private val lock = Mutex()
    }
}

