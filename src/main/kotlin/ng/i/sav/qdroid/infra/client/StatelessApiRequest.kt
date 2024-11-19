package ng.i.sav.qdroid.infra.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ng.i.sav.qdroid.bot.config.BotConfiguration
import ng.i.sav.qdroid.bot.event.MessageAuditResultHandler
import ng.i.sav.qdroid.infra.model.*
import ng.i.sav.qdroid.infra.util.Tools
import ng.i.sav.qdroid.log.Slf4kt
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitExchange
import org.springframework.web.util.UriComponentsBuilder
import java.io.File
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset

@Component
class StatelessApiRequest(
    configuration: BotConfiguration
) : ApiRequest {
    private val appId: String = configuration.appId
    private val token: String = configuration.token
    private val apiHost: String = configuration.apiHost


    private fun throwAtLeastOne(): Nothing {
        throw IllegalArgumentException("At least one arg needed.")
    }

    override suspend fun getUsersMe(): User {
        return get(ApiPath.GET_USERS_ME)
    }

    override suspend fun getUsersMeGuilds(before: String?, after: String?, limit: Int): ArrayList<Guild> {
        if (Tools.allBlank(before, after)) {
            throwAtLeastOne()
        }
        val map = hashMapOf<String, Any>()
        val l = if (limit < 1 || limit > 100) 100 else limit
        map.putNotNull("before", before).putNotNull("after", after).putNotNull("limit", l)
        return get(ApiPath.GET_USERS_ME_GUILDS, params = map)
    }

    override suspend fun getGuilds(guildId: String): Guild {
        return get(ApiPath.GET_GUILDS, guildId)
    }

    override suspend fun getGuildsChannels(guildId: String): List<Channel> {
        return get(ApiPath.GET_GUILDS_CHANNELS, guildId)
    }

    override suspend fun getChannels(channelId: String): ChannelDetail {
        return get(ApiPath.GET_CHANNELS, channelId)
    }

    override suspend fun createGuildsChannels(guildId: String, channel: CreateChannel): Channel {
        return post(ApiPath.CREATE_GUILDS_CHANNELS, channel, uriVariables = arrayOf(guildId))
    }

    override suspend fun modifyChannels(channelId: String, modifyChannel: ModifyChannel): Channel {
        return patch(ApiPath.MODIFY_CHANNELS, modifyChannel, channelId)
    }

    override suspend fun deleteChannels(channelId: String) {
        return delete(ApiPath.DELETE_CHANNELS, channelId, body = Unit)
    }

    override suspend fun getChannelsOnlineNums(channelId: String): OnlineNums {
        return get(ApiPath.GET_CHANNELS_ONLINE_NUMS, uriVariables = arrayOf(channelId))
    }

    override suspend fun getGuildsMembers(guildId: String, after: String, limit: Int): List<Member> {
        val l = if (limit < 1 || limit > 400) 1 else limit
        val params = hashMapOf<String, Any>("after" to after, "limit" to l)
        return get(ApiPath.GET_GUILDS_MEMBERS, guildId, params = params)
    }

    override suspend fun getGuildsMember(guildId: String, userId: String): Member {
        return get(ApiPath.GET_GUILDS_MEMBER, guildId, userId)
    }

    override suspend fun getGuildsMembersRoles(guildId: String, roleId: String, startIndex: String, limit: Int): RolesResp {
        val l = if (limit < 1 || limit > 400) 1 else limit
        val params = hashMapOf<String, Any>("start_index" to startIndex, "limit" to l)
        return get(ApiPath.GET_GUILDS_MEMBERS_ROLES, guildId, roleId, params = params)
    }

    override suspend fun deleteGuildsMembers(
        guildId: String, userId: String, addBlacklist: Boolean, deleteHistoryMsgDays: Int
    ) {
        val d = if (arrayOf(-1, 0, 3, 7, 15, 30).contains(deleteHistoryMsgDays)) deleteHistoryMsgDays else 0
        hashMapOf("add_blacklist" to addBlacklist, "delete_history_msg_days" to d)
        return delete(ApiPath.DELETE_GUILDS_MEMBERS, guildId, userId, body = Unit)
    }

    override suspend fun getGuildsRoles(guildId: String): RolesListResp {
        return get(ApiPath.GET_GUILDS_ROLES, guildId)
    }

    override suspend fun createGuildsRoles(guildId: String, name: String?, color: UInt?, hoist: Int?): CreatedRole {
        val b = hashMapOf<String, Any>().putNotNull("name", name).putNotNull("color", color).putNotNull("hoist", hoist)
        return post(ApiPath.CREATE_GUILDS_ROLES, b, guildId)
    }

    override suspend fun modifyGuildsRoles(
        guildId: String, roleId: String, name: String?, color: UInt?, hoist: Int?
    ): ChangedRole {
        val b = hashMapOf<String, Any>().putNotNull("name", name).putNotNull("color", color).putNotNull("hoist", hoist)
        return patch(ApiPath.MODIFY_GUILDS_ROLES, b, guildId, roleId)
    }

    override suspend fun deleteGuildsRoles(guildId: String, roleId: String) {
        return delete(ApiPath.DELETE_GUILDS_ROLES, guildId, roleId, body = Unit)
    }

    override suspend fun addGuildsRolesMembers(guildId: String, userId: String, roleId: String, channel: Channel) {
        return put(ApiPath.ADD_GUILDS_ROLES_MEMBERS, channel, guildId, userId, roleId)
    }

    override suspend fun deleteGuildsRolesMembers(guildId: String, roleId: String, userId: String, channel: Channel) {
        return delete(ApiPath.DELETE_GUILDS_ROLES_MEMBERS, guildId, roleId, userId, body = channel)
    }

    override suspend fun getChannelsMembersPermissions(channelId: String, userId: String): ChannelPermissions {
        return get(ApiPath.GET_CHANNELS_MEMBERS_PERMISSIONS, channelId, userId)
    }

    override suspend fun modifyChannelsMembersPermissions(
        channelId: String,
        userId: String,
        add: UInt?,
        remove: UInt?
    ) {
        val b = permissionsMap(add, remove)
        return put(ApiPath.MODIFY_CHANNELS_MEMBERS_PERMISSIONS, b, channelId, userId)
    }

    private fun permissionsMap(add: UInt?, remove: UInt?): HashMap<String, Any> {
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
        return b
    }

    override suspend fun getChannelsRolesPermissions(channelId: String, roleId: String): ChannelPermissions {
        return get(ApiPath.GET_CHANNELS_ROLES_PERMISSIONS, channelId, roleId)
    }

    override suspend fun modifyChannelsRolesPermissions(
        channelId: String,
        roleId: String,
        add: UInt?,
        remove: UInt?
    ): ChannelPermissions {
        val b = permissionsMap(add, remove)
        return put(ApiPath.MODIFY_CHANNELS_ROLES_PERMISSIONS, b, channelId, roleId)
    }

    override suspend fun getChannelsMessages(channelId: String, messageId: String): Message {
        return get(ApiPath.GET_CHANNELS_MESSAGES, channelId, messageId)
    }

    override suspend fun postChannelsMessages(
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
        val body = messagesMap(content, embed, ark, image, fileImage, markdown, messageReference, msgId, eventId, keyboard)
        return if (fileImage == null) {
            post(ApiPath.POST_CHANNELS_MESSAGES, body, channelId)
        } else {
            body.putNotNull("file_image", fileImage.inputStream())
            val headers = HttpHeaders()
            headers.contentType = MediaType.MULTIPART_FORM_DATA
            post(ApiPath.POST_CHANNELS_MESSAGES, body, headers = headers, uriVariables = arrayOf(channelId))
        }
    }

    override suspend fun postChannelsMessagesWithAudited(
        messageAuditResultHandler: MessageAuditResultHandler,
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
    ): Message = runCatching {
        postChannelsMessages(
            channelId,
            content,
            embed,
            ark,
            messageReference,
            image,
            msgId,
            eventId,
            markdown,
            keyboard,
            fileImage
        )
    }.getOrElse {
        if (it is ApiRequestFailure) {
            if (it.errorData?.code == 304023) {
                val data = it.errorData.data
                data as HashMap<*, *>
                val auditId = (data["message_audit"] as HashMap<*, *>)["audit_id"] as String
                return@getOrElse messageAuditResultHandler.onAudited(auditId)?.second?.messageId?.let { id ->
                    getChannelsMessages(
                        channelId,
                        id
                    )
                } ?: throw it
            }
        }
        throw it
    }


    override suspend fun deleteChannelsMessages(channelId: String, messageId: String, hideTip: Boolean) {
        return delete(
            ApiPath.DELETE_CHANNELS_MESSAGES,
            channelId,
            messageId,
            body = Unit,
            params = hashMapOf("hidetip" to hideTip)
        )
    }

    override suspend fun getGuildsMessageSetting(guildId: String): MessageSetting {
        return get(ApiPath.GET_GUILDS_MESSAGE_SETTING, guildId)
    }

    override suspend fun createUsersDms(recipientId: String, sourceGuildId: String): DirectMessage {
        val b = hashMapOf("recipient_id" to recipientId, "source_guild_id" to sourceGuildId)
        return post(ApiPath.CREATE_USERS_DMS, body = b)
    }

    override suspend fun postDmsMessages(
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
        val body = messagesMap(content, embed, ark, image, fileImage, markdown, messageReference, msgId, eventId, keyboard)
        return if (fileImage == null) {
            post(ApiPath.POST_CHANNELS_MESSAGES, body, guildId)
        } else {
            body.putNotNull("file_image", fileImage.inputStream())
            val headers = HttpHeaders()
            headers.contentType = MediaType.MULTIPART_FORM_DATA
            post(ApiPath.POST_DMS_MESSAGES, body, guildId, headers = headers)
        }
    }

    private fun messagesMap(
        content: String?,
        embed: MessageEmbed?,
        ark: MessageArk?,
        image: String?,
        fileImage: File?,
        markdown: MessageMarkdown?,
        messageReference: MessageReference?,
        msgId: String?,
        eventId: String?,
        keyboard: MessageKeyboard?
    ): HashMap<String, Any> {
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
        return body
    }

    override suspend fun deleteDmsMessages(guildId: String, messageId: String, hideTip: Boolean) {
        return delete(
            ApiPath.DELETE_DMS_MESSAGES,
            guildId, messageId,
            body = Unit,
            params = hashMapOf("hidetip" to hideTip)
        )
    }

    override suspend fun setGuildsMute(guildId: String, muteEndTimestamp: LocalDateTime?, muteSeconds: Duration) {
        val b = hashMapOf<String, Any>()
        b.putNotNull("mute_end_timestamp", muteEndTimestamp?.toEpochSecond(ZoneOffset.UTC))
        b.putNotNull("mute_seconds", muteSeconds.seconds)
        return patch(ApiPath.SET_GUILDS_MUTE, b, guildId)
    }

    override suspend fun setGuildsMuteList(
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

    override suspend fun setGuildsMembersMute(
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

    override suspend fun createGuildsAnnounces(
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

    override suspend fun deleteGuildsAnnounces(guildId: String, messageId: String) {
        return delete(ApiPath.DELETE_GUILDS_ANNOUNCES, guildId, messageId, body = Unit)
    }

    override suspend fun addChannelsPins(channelId: String, messageId: String): PinsMessage {
        return put(ApiPath.ADD_CHANNELS_PINS, body = Unit, channelId, messageId)
    }

    override suspend fun deleteChannelsPins(channelId: String, messageId: String) {
        return delete(ApiPath.DELETE_CHANNELS_PINS, channelId, messageId, body = Unit)
    }

    override suspend fun getChannelsPins(channelId: String): PinsMessage {
        return get(ApiPath.GET_CHANNELS_PINS, channelId)
    }

    override suspend fun getChannelsSchedules(channelId: String, since: LocalDateTime?): List<Schedule> {
        val p = hashMapOf<String, Any>()
        p.putNotNull("since", since?.toEpochSecond(ZoneOffset.UTC)?.times(1000))
        return get(ApiPath.GET_CHANNELS_SCHEDULES, channelId, params = p)
    }

    override suspend fun getChannelsSchedule(channelId: String, scheduleId: String): Schedule {
        return get(ApiPath.GET_CHANNELS_SCHEDULE, channelId, scheduleId)
    }

    override suspend fun createChannelsSchedules(channelId: String, schedule: Schedule): Schedule {
        val b = hashMapOf("schedule" to schedule)
        return post(ApiPath.CREATE_CHANNELS_SCHEDULES, b, channelId)
    }

    override suspend fun modifyChannelsSchedules(channelId: String, scheduleId: String, schedule: Schedule) {
        val b = hashMapOf("schedule" to schedule)
        return patch(ApiPath.MODIFY_CHANNELS_SCHEDULES, b, channelId, scheduleId)
    }

    override suspend fun deleteChannelsSchedules(channelId: String, scheduleId: String) {
        return delete(ApiPath.DELETE_CHANNELS_SCHEDULES, channelId, scheduleId, body = Unit)
    }

    override suspend fun setChannelsMessagesReactions(channelId: String, messageId: String, emoji: Emoji) {
        return put(
            ApiPath.SET_CHANNELS_MESSAGES_REACTIONS,
            Unit,
            channelId,
            messageId,
            emoji.type.type.toString(),
            emoji.id
        )
    }

    override suspend fun deleteChannelsMessagesReactions(channelId: String, messageId: String, emoji: Emoji) {
        return delete(
            ApiPath.DELETE_CHANNELS_MESSAGES_REACTIONS,
            channelId,
            messageId,
            emoji.type.type.toString(),
            emoji.id,
            body = Unit
        )
    }

    override suspend fun getChannelsMessagesReactions(
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

    override suspend fun setChannelsAudio(channelId: String, audioControl: AudioControl) {
        post<AudioControl, Any>(ApiPath.SET_CHANNELS_AUDIO, audioControl, channelId)
    }

    override suspend fun putChannelsMic(channelId: String) {
        put<Unit, Any>(ApiPath.PUT_CHANNELS_MIC, Unit, channelId)
    }

    override suspend fun deleteChannelsMic(channelId: String) {
        delete<Unit, Any>(ApiPath.DELETE_CHANNELS_MIC, channelId, body = Unit)
    }

    override suspend fun getChannelsThreads(channelId: String): ThreadsResp {
        return get(ApiPath.GET_CHANNELS_THREADS, channelId)
    }

    override suspend fun getChannelsThread(channelId: String, threadId: String): ThreadResp {
        return get(ApiPath.GET_CHANNELS_THREAD, channelId, threadId)
    }

    override suspend fun createChannelsThreads(
        channelId: String,
        title: String,
        content: String,
        format: Format
    ): CreateThreadResp {
        val b = hashMapOf<String, Any>("title" to title, "content" to content, "format" to format.value)
        return put(ApiPath.CREATE_CHANNELS_THREADS, b, channelId)
    }


    override suspend fun deleteChannelsThreads(channelId: String, threadId: String) {
        return delete(ApiPath.DELETE_CHANNELS_THREADS, channelId, threadId, body = Unit)
    }

    override suspend fun getGuildsApiPermission(guildId: String): List<APIPermission> {
        return get(ApiPath.GET_GUILDS_API_PERMISSION, guildId)
    }

    override suspend fun createGuildsApiPermissionDemand(
        guildId: String, channelId: String, apiIdentify: APIPermissionDemandIdentify, desc: String
    ): APIPermissionDemand {
        val b = hashMapOf("channel_id" to channelId, "api_identify" to apiIdentify, "desc" to desc)
        return post(ApiPath.CREATE_GUILDS_API_PERMISSION_DEMAND, b, guildId)
    }

    override suspend fun getGateway(): String {
        return get<Map<String, Any>>(ApiPath.GET_GATEWAY)["url"]?.toString()!!
    }

    override suspend fun getGatewayBot(): WebsocketApi {
        return get(ApiPath.GET_GATEWAY_BOT)

    }

    override suspend fun postUsersMessage(): Message {
        TODO("Not yet implemented")
    }

    override suspend fun postGroupsMessage(): Message {
        TODO("Not yet implemented")
    }


    private suspend inline fun <reified T> get(
        path: String, vararg uriVariables: String, params: Map<String, Any>? = null, headers: HttpHeaders? = null
    ): T {
        return noBody(HttpMethod.GET, path, params, object : ParameterizedTypeReference<T>() {}, headers, *uriVariables)
    }

    private suspend inline fun <B, reified T> delete(
        path: String, vararg uriVariables: String,
        body: B, params: Map<String, Any>? = null, headers: HttpHeaders? = null
    ): T {
        return withBody(
            HttpMethod.DELETE, path, params, body, object : ParameterizedTypeReference<T>() {}, headers, *uriVariables
        )
    }

    private suspend inline fun <B, reified T> post(
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

    private suspend inline fun <B, reified T> patch(
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

    private suspend inline fun <B, reified T> put(
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

    private suspend inline fun <reified T> noBody(
        method: HttpMethod,
        path: String,
        params: Map<String, Any>? = null,
        responseType: ParameterizedTypeReference<T>,
        headers: HttpHeaders? = null,
        vararg uriVariables: String
    ): T {
        val url = "${apiHost.removeSuffix("/")}$path"
        val builder = UriComponentsBuilder.fromUriString(url)
        params?.forEach { (k, v) ->
            builder.queryParam(k, v)
        }
        return withContext(Dispatchers.IO) {
            WebClient.create(builder.buildAndExpand(*uriVariables).toUriString()).method(method)
                .headers {
                    if (headers != null) it.addAll(headers)
                    if (it[HttpHeaders.AUTHORIZATION] == null) it[HttpHeaders.AUTHORIZATION] = getAuthToken()
                }.awaitExchange {
                    it.bodyToMono(responseType).block() ?: throw ApiRequestFailure("")
                }
        }


        /*return restTemplate.exchange(
            builder.buildAndExpand(*uriVariables).toUri(), method, HttpEntity<Unit>(HttpHeaders().apply {
                if ((headers?.get(HttpHeaders.AUTHORIZATION)) == null) set(
                    HttpHeaders.AUTHORIZATION, getAuthToken()
                )
                if (headers != null) addAll(headers)
            }), responseType
        ).body.also {
            log.info("Send request: GET {}, response with: {}", path, it)
        } ?: throw ApiRequestFailure("")*/
    }

    private suspend inline fun <B, reified T> withBody(
        method: HttpMethod,
        path: String,
        params: Map<String, Any>? = null,
        body: B,
        responseType: ParameterizedTypeReference<T>,
        headers: HttpHeaders? = null,
        vararg uriVariables: String
    ): T {
        val url = "${apiHost.removeSuffix("/")}$path"
        val builder = UriComponentsBuilder.fromUriString(url)
        params?.forEach { (k, v) ->
            builder.queryParam(k, v)
        }
        return withContext(Dispatchers.IO) {
            WebClient.create(builder.buildAndExpand(*uriVariables).toUriString()).method(method)
                .headers {
                    if (headers != null) it.addAll(headers)
                    if (it[HttpHeaders.AUTHORIZATION] == null) it[HttpHeaders.AUTHORIZATION] = getAuthToken()
                }.bodyValue(body!!).awaitExchange {
                    when (it.statusCode()) {
                        HttpStatus.BAD_REQUEST,
                        HttpStatus.UNAUTHORIZED,
                        HttpStatus.TOO_MANY_REQUESTS,
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        HttpStatus.GATEWAY_TIMEOUT -> {
                            val errorData = it.bodyToMono(ApiRequestFailure.ErrorData::class.java).block()
                            throw ApiRequestFailure(errorData?.message, it.statusCode(), errorData)
                        }

                        else -> {
                            it.bodyToMono(responseType).block() ?: throw ApiRequestFailure("")
                        }
                    }
                }
        }

        /*restTemplate.execute(
            builder.buildAndExpand(*uriVariables).toUri(), method,
            restTemplate.httpEntityCallback<T>(HttpEntity(body, HttpHeaders().apply {
                if ((headers?.get(HttpHeaders.AUTHORIZATION)) == null) set(
                    HttpHeaders.AUTHORIZATION, getAuthToken()
                )
                if (headers != null) addAll(headers)
            }), responseType.type),
            {
                when (it.statusCode) {
                    HttpStatus.ACCEPTED, HttpStatus.CREATED -> {}

                    HttpStatus.INTERNAL_SERVER_ERROR,
                    HttpStatus.GATEWAY_TIMEOUT -> {

                    }

                    else -> {
                        restTemplate.responseEntityExtractor<T>(responseType.type).extractData(it)
                    }
                }
            },
        )

        return restTemplate.exchange(
            builder.buildAndExpand(*uriVariables).toUri(), method, HttpEntity(body, HttpHeaders().apply {
                if ((headers?.get(HttpHeaders.AUTHORIZATION)) == null) set(
                    HttpHeaders.AUTHORIZATION, getAuthToken()
                )
                if (headers != null) addAll(headers)
            }), responseType
        ).body.also {
            log.debug("Send request: POST {}, body: {}, response with: {}", path, body, it)
        } ?: throw ApiRequestFailure("")*/
    }


    private fun getAuthToken(): String = "Bot $appId.$token"

    private fun <K, V> MutableMap<K, V>.putNotNull(key: K, value: V?): MutableMap<K, V> {
        if (value != null) this[key] = value
        return this
    }

    companion object {
        private val log = Slf4kt.getLogger(StatelessApiRequest::class.java)
    }

}
