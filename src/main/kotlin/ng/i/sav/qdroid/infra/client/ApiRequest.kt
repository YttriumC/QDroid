package ng.i.sav.qdroid.infra.client

import ng.i.sav.qdroid.bot.event.MessageAuditResultHandler
import ng.i.sav.qdroid.infra.model.*
import java.io.File
import java.time.Duration
import java.time.LocalDateTime

interface ApiRequest {
    //user
    /**
     * 获取用户详情
     * @return [User]
     * */
    suspend fun getUsersMe(): User

    /**
     * 获取用户频道列表
     * @param before    string	读此 guild id 之前的数据	before 设置时， 先反序，再分页
     * @param after    string	读此 guild id 之后的数据	after 和 before 同时设置时， after 参数无效
     * @param limit    int	每次拉取多少条数据	默认 100, 最大 100
     * @return [User]
     * */
    suspend fun getUsersMeGuilds(before: String? = null, after: String? = "0", limit: Int = 100): ArrayList<Guild>

    // guild
    /**
     * 获取频道详情
     * @return [Guild]
     * */
    suspend fun getGuilds(guildId: String): Guild

    // channel
    /**
     * 获取子频道列表
     * @return [Channel]数组
     * */
    suspend fun getGuildsChannels(guildId: String): List<Channel>

    /**
     * 获取子频道详情
     * @return [ChannelDetail]
     */
    suspend fun getChannels(channelId: String): ChannelDetail

    /**
     * 创建子频道
     */
    suspend fun createGuildsChannels(guildId: String, channel: CreateChannel): Channel

    /**
     * 修改子频道
     */
    suspend fun modifyChannels(channelId: String, modifyChannel: ModifyChannel): Channel

    /**
     * 删除子频道
     */
    suspend fun deleteChannels(channelId: String)

    /**
     * 获取在线成员数
     */
    suspend fun getChannelsOnlineNums(channelId: String): OnlineNums

    /**
     * 获取频道成员列表
     * @param after    string	上一次回包中最后一个member的user id， 如果是第一次请求填 0，默认为 0
     * @param limit    uint32	分页大小，1-400，默认是 1。成员较多的频道尽量使用较大的limit值，以减少请求数
     *
     * 1. 在每次翻页的过程中，可能会返回上一次请求已经返回过的member信息，需要调用方自己根据user id来进行去重。
     * 2. 每次返回的member数量与limit不一定完全相等。翻页请使用最后一个member的user id作为下一次请求的after参数，直到回包为空，拉取结束。
     * @return [Member] 数组
     */
    suspend fun getGuildsMembers(guildId: String, after: String = "0", limit: Int = 1): List<Member>

    /**
     * 获取频道身份组成员列表
     *
     * 1. 每次返回的member数量与limit不一定完全相等。特定管理身份组下的成员可能存在一次性返回全部的情况
     * @return [RolesResp]
     * */
    suspend fun getGuildsMembersRoles(
        guildId: String,
        roleId: String,
        startIndex: String = "0",
        limit: Int = 1
    ): ng.i.sav.qdroid.infra.model.RolesResp

    /**
     * 获取成员详情
     * @return [Member]
     * */
    suspend fun getGuildsMember(guildId: String, userId: String): Member

    /**
     * 删除频道成员
     * 用于删除 guild_id 指定的频道下的成员 user_id。
     *
     * - 需要使用的 token 对应的用户具备踢人权限。如果是机器人，要求被添加为管理员。
     * - 操作成功后，会触发频道成员删除事件。
     * - 无法移除身份为管理员的成员
     *
     * @param addBlacklist    bool	删除成员的同时，将该用户添加到频道黑名单中
     * @param deleteHistoryMsgDays    int	删除成员的同时，撤回该成员的消息，可以指定撤回消息的时间范围 注：消息撤回时间范围仅支持固定的天数：3，7，15，30。 特殊的时间范围：-1: 撤回全部消息。默认值为0不撤回任何消息。
     * */
    suspend fun deleteGuildsMembers(
        guildId: String,
        userId: String,
        addBlacklist: Boolean = false,
        deleteHistoryMsgDays: Int = 0
    )

    //Role
    /**
     * 获取频道身份组列表
     * 用于在guild_id 指定的频道下创建一个身份组。
     * @return [RolesListResp]
     */
    suspend fun getGuildsRoles(guildId: String): RolesListResp

    /**
     * - 需要使用的 token 对应的用户具备创建身份组权限。如果是机器人，要求被添加为管理员。
     * - 参数为非必填，但至少需要传其中之一，默认为空或 0
     * @param name    string	名称(非必填)
     * @param color    uint32	ARGB 的 HEX 十六进制颜色值转换后的十进制数值(非必填)
     * @param hoist    int32	在成员列表中单独展示: 0-否, 1-是(非必填)
     * @return [CreatedRole]
     * */
    suspend fun createGuildsRoles(guildId: String, name: String? = null, color: UInt? = null, hoist: Int? = null): CreatedRole

    /**
     * 用于修改频道 guild_id 下 role_id 指定的身份组。
     *
     * - 需要使用的 token 对应的用户具备修改身份组权限。如果是机器人，要求被添加为管理员。
     * - 接口会修改传入的字段，不传入的默认不会修改，至少要传入一个参数
     * @param name    string	名称(非必填)
     * @param color    uint32	ARGB 的 HEX 十六进制颜色值转换后的十进制数值(非必填)
     * @param hoist    int32	在成员列表中单独展示: 0-否, 1-是(非必填)
     * @return [ChangedRole]
     * */
    suspend fun modifyGuildsRoles(
        guildId: String,
        roleId: String,
        name: String? = null,
        color: UInt? = null,
        hoist: Int? = null
    ): ChangedRole

    /**
     * 删除频道身份组
     *
     * 用于删除频道guild_id下 role_id 对应的身份组。
     *
     * - 需要使用的 token 对应的用户具备删除身份组权限。如果是机器人，要求被添加为管理员
     * */
    suspend fun deleteGuildsRoles(guildId: String, roleId: String)

    /**
     * 创建频道身份组成员
     * 用于将频道guild_id下的用户 user_id 添加到身份组 role_id 。
     *
     * - 需要使用的 token 对应的用户具备增加身份组成员权限。如果是机器人，要求被添加为管理员。
     * - 如果要增加的身份组 ID 是5-子频道管理员，需要增加 channel 对象来指定具体是哪个子频道
     * */
    suspend fun addGuildsRolesMembers(guildId: String, userId: String, roleId: String, channel: Channel)

    /**
     * 删除频道身份组成员
     *
     * 用于将 用户 user_id 从 频道 guild_id 的 role_id 身份组中移除。
     *
     * - 需要使用的 token 对应的用户具备删除身份组成员权限。如果是机器人，要求被添加为管理员。
     * - 如果要删除的身份组 ID 是5-子频道管理员，需要增加 channel 对象来指定具体是哪个子频道。
     * */
    suspend fun deleteGuildsRolesMembers(guildId: String, roleId: String, userId: String, channel: Channel)
    //ChannelPermissions
    /**
     * 获取子频道用户权限
     * 用于获取 子频道[channel_id][channelId] 下用户 [user_id][userId] 的权限。
     *
     * - 获取子频道用户权限。
     * - 要求操作人具有管理子频道的权限，如果是机器人，则需要将机器人设置为管理员
     *
     * @return [ChannelPermissions]
     * */
    suspend fun getChannelsMembersPermissions(channelId: String, userId: String): ChannelPermissions

    /**
     * 修改子频道权限
     *
     * 用于修改子频道 channel_id 下用户 user_id 的权限。
     *
     * - 要求操作人具有管理子频道的权限，如果是机器人，则需要将机器人设置为管理员。
     * - 参数包括[add]和[remove]两个字段，分别表示授予的权限以及删除的权限。要授予用户权限即把[add]对应位置 1，删除用户权限即把[remove]对应位置 1。当两个字段同一位都为 1，表现为删除权限。
     * - 本接口不支持修改可管理子频道权限
     *
     * @param add    string	字符串形式的位图表示赋予用户的权限
     * @param remove    string	字符串形式的位图表示删除用户的权限
     * */

    suspend fun modifyChannelsMembersPermissions(
        channelId: String,
        userId: String,
        add: UInt?,
        remove: UInt?
    )

    /**
     * 获取子频道身份组权限
     *
     * 用于获取子频道 channel_id 下身份组 role_id 的权限。
     *
     * - 要求操作人具有管理子频道的权限，如果是机器人，则需要将机器人设置为管理员。
     * @return [ChannelPermissions]
     * */
    suspend fun getChannelsRolesPermissions(channelId: String, roleId: String): ChannelPermissions

    /**
     * 修改子频道身份组权限
     *
     * 用于修改子频道 channel_id 下身份组 role_id 的权限。
     *
     * - 要求操作人具有管理子频道的权限，如果是机器人，则需要将机器人设置为管理员。
     * - 参数包括add和remove两个字段，分别表示授予的权限以及删除的权限。要授予身份组权限即把[add]对应位置 1，删除身份组权限即把[remove]对应位置 1。当两个字段同一位都为 1，表现为删除权限。
     * - 本接口不支持修改可管理子频道权限。
     * @param add    string	字符串形式的位图表示赋予用户的权限
     * @param remove    string	字符串形式的位图表示删除用户的权限
     * @return [ChannelPermissions]
     * */
    suspend fun modifyChannelsRolesPermissions(
        channelId: String, roleId: String,
        add: UInt?,
        remove: UInt?
    ): ChannelPermissions
    // Message

    /**
     * 获取指定消息
     * */
    suspend fun getChannelsMessages(channelId: String, messageId: String): Message

    /**
     * 用于向 [channel_id][channelId] 指定的子频道发送消息。
     *
     * - 要求操作人在该子频道具有发送消息的权限。
     * - 主动消息在频道主或管理设置了情况下，按设置的数量进行限频。在未设置的情况遵循如下限制:
     *     - 主动推送消息，默认每天往每个子频道可推送的消息数是 20 条，超过会被限制。
     *     - 主动推送消息在每个频道中，每天可以往 2 个子频道推送消息。超过后会被限制。
     * - 不论主动消息还是被动消息，在一个子频道中，每 1s 只能发送 5 条消息。
     * - 被动回复消息有效期为 5 分钟。超时会报错。
     * - 发送消息接口要求机器人接口需要连接到 websocket 上保持在线状态
     * - 有关主动消息审核，可以通过 Intents 中审核事件 MESSAGE_AUDIT 返回 [MessageAudited] 对象获取结果。
     *
     * - 主动消息：发送消息时，未填充 msg_id/event_id 字段的消息。
     * - 被动消息：发送消息时，填充了 msg_id/event_id 字段的消息。
     * msg_id 和 event_id 两个字段任意填一个即为被动消息。接口使用此 msg_id/event_id 拉取用户的消息或事件，
     * 同时判断用户消息或事件的发送时间，如果超过被动消息回复时效，将会不允许发送该消息。
     * 目前支持被动回复的事件类型有:
     * GUILD_MEMBER_ADD  GUILD_MEMBER_UPDATE  GUILD_MEMBER_REMOVE
     * MESSAGE_REACTION_ADD  MESSAGE_REACTION_REMOVE  FORUM_THREAD_CREATE
     * FORUM_THREAD_UPDATE  FORUM_THREAD_DELETE  FORUM_POST_CREATE
     * FORUM_POST_DELETE  FORUM_REPLY_CREATE  FORUM_REPLY_DELETE
     *
     * @param content    string	选填，消息内容，文本内容，支持内嵌格式
     *
     * 利用 content 字段发送内嵌格式的消息。
     * - 内嵌格式仅在 content 中会生效，在 Ark 和 Embed 中不生效。
     * - 为了区分是文本还是内嵌格式，消息抄送和发送会对消息内容进行相关的转义，参考 转义内容
     * @param embed    [MessageEmbed]	选填，embed 消息，一种特殊的 ark，详情参考Embed消息
     * @param ark    [MessageArk] ark消息对象	选填，ark 消息
     *
     * 通过指定 ark 字段发送模板消息。
     * - 要求操作人在该子频道具有发送消息和 对应ARK 模板 的权限。
     * - 调用前需要先申请消息模板，这一步会得到一个模板 id，在请求时填在 ark.template_id 上。
     * - 发送成功之后，会触发一个创建消息的事件。
     * - 可用模板参考可用模板。
     * @param messageReference    [MessageReference] 引用消息对象	选填，引用消息
     *
     * - 只支持引用机器人自己发送到的消息以及用户@机器人产生的消息。
     * - 发送成功之后，会触发一个创建消息的事件
     * @param image    string	选填，图片url地址，平台会转存该图片，用于下发图片消息
     * @param msgId    string	选填，要回复的消息id(Message.id), 在 AT_CREATE_MESSAGE 事件中获取。
     * @param eventId    string	选填，要回复的事件id, 在各事件对象中获取。
     * @param markdown    [MessageMarkdown] markdown 消息对象	选填，markdown 消息
     * @param keyboard [MessageKeyboard] 通过指定 keyboard 字段发送带按钮的消息，支持 keyboard 模版 和 自定义 keyboard 两种请求格式。
     *  - 要求操作人在该子频道具有发送消息和 对应消息按钮组件 的权限。
     *  - 请求参数 keyboard 模版 和 自定义 keyboard 只能单一传值。
     *  - keyboard 模版
     *     - 调用前需要先申请消息按钮组件模板，这一步会得到一个模板 id，在请求时填在 keyboard 字段上。
     *     - 申请消息按钮组件模板需要提供响应的 json，具体格式参考 InlineKeyboard。
     *  - 仅 markdown 消息支持消息按钮。
     * @param fileImage [File] 图片文件。form-data 支持直接通过文件上传的方式发送图片。
     * @return [Message]
     * */
    suspend fun postChannelsMessages(
        channelId: String,
        content: String? = null,
        embed: MessageEmbed? = null,
        ark: MessageArk? = null,
        messageReference: MessageReference? = null,
        image: String? = null,
        msgId: String? = null,
        eventId: String? = null,
        markdown: MessageMarkdown? = null,
        keyboard: MessageKeyboard? = null,
        fileImage: File? = null
    ): Message


    suspend fun postChannelsMessagesWithAudited(
        messageAuditResultHandler: MessageAuditResultHandler,
        channelId: String,
        content: String? = null,
        embed: MessageEmbed? = null,
        ark: MessageArk? = null,
        messageReference: MessageReference? = null,
        image: String? = null,
        msgId: String? = null,
        eventId: String? = null,
        markdown: MessageMarkdown? = null,
        keyboard: MessageKeyboard? = null,
        fileImage: File? = null
    ): Message

    /**
     * 撤回消息
     *
     * @param hideTip    bool	选填，是否隐藏提示小灰条，true 为隐藏，false 为显示。默认为false
     * */
    suspend fun deleteChannelsMessages(channelId: String, messageId: String, hideTip: Boolean = false)

    // Message setting

    /**
     * 获取频道消息频率设置
     *
     * @return [MessageSetting]
     * */
    suspend fun getGuildsMessageSetting(guildId: String): MessageSetting

    // Direct Message

    /**
     * 创建私信会话
     *
     * @param sourceGuildId    string	源频道 id
     * @return [DirectMessage]
     * */
    suspend fun createUsersDms(recipientId: String, sourceGuildId: String): DirectMessage

    /**
     * 发送私信
     *
     * 用于发送私信消息，前提是已经创建了私信会话。
     *
     * - 私信的 guild_id 在创建私信会话时以及私信消息事件中获取。
     * - 私信场景下，每个机器人每天可以对一个用户发 2 条主动消息。
     * - 私信场景下，每个机器人每天累计可以发 200 条主动消息。
     * - 私信场景下，被动消息没有条数限制。
     * @param guildId 私信的 guild_id 在创建私信会话时以及私信消息事件中获取。
     * @param content 参见 [postChannelsMessages]
     * @param embed 参见 [postChannelsMessages]
     * @param ark 参见 [postChannelsMessages]
     * @param messageReference 参见 [postChannelsMessages]
     * @param image 参见 [postChannelsMessages]
     * @param msgId 参见 [postChannelsMessages]
     * @param eventId 参见 [postChannelsMessages]
     * @param markdown 参见 [postChannelsMessages]
     * @param keyboard 参见 [postChannelsMessages]
     * @param fileImage 参见 [postChannelsMessages]
     * @return [Message]
     * */
    suspend fun postDmsMessages(
        guildId: String,
        content: String? = null,
        embed: MessageEmbed? = null,
        ark: MessageArk? = null,
        messageReference: MessageReference? = null,
        image: String? = null,
        msgId: String? = null,
        eventId: String? = null,
        markdown: MessageMarkdown? = null,
        keyboard: MessageKeyboard? = null,
        fileImage: File? = null
    ): Message

    /**
     * 撤回私信
     *
     * @param hideTip    bool	选填，是否隐藏提示小灰条，true 为隐藏，false 为显示。默认为false
     * @param messageId
     * */
    suspend fun deleteDmsMessages(guildId: String, messageId: String, hideTip: Boolean = false)

    // Prohibition
    /**
     * 禁言全员
     * 用于将频道的全体成员（非管理员）禁言。
     *
     * - 需要使用的 token 对应的用户具备管理员权限。如果是机器人，要求被添加为管理员。
     * - 该接口同样支持解除全员禁言，将[muteEndTimestamp]或[muteSeconds]传值为字符串'0'即可。
     *
     * @param muteEndTimestamp    string	禁言到期时间戳，绝对时间戳，单位：秒（与 mute_seconds 字段同时赋值的话，以该字段为准）
     * @param muteSeconds    string	禁言多少秒（两个字段二选一，默认以 mute_end_timestamp 为准）
     * */
    suspend fun setGuildsMute(guildId: String, muteEndTimestamp: LocalDateTime?, muteSeconds: Duration = Duration.ZERO)

    /**
     * 禁言指定成员
     *
     * 用于禁言频道 guild_id 下的成员 user_id。
     *
     * - 需要使用的 token 对应的用户具备管理员权限。如果是机器人，要求被添加为管理员。
     * - 该接口同样可用于解除禁言，具体使用见[解除指定成员禁言][setGuildsMute]。
     * @param muteEndTimestamp    string	禁言到期时间戳，绝对时间戳，单位：秒（与 [muteSeconds] 字段同时赋值的话，以该字段为准）
     * @param muteSeconds    string	禁言多少秒（两个字段二选一，默认以 [muteEndTimestamp] 为准）
     * */
    suspend fun setGuildsMembersMute(
        guildId: String,
        userId: String,
        muteEndTimestamp: LocalDateTime?,
        muteSeconds: Duration = Duration.ZERO
    )

    /**
     * 禁言批量成员
     *
     * 用于将频道的指定批量成员（非管理员）禁言。
     * - 需要使用的 token 对应的用户具备管理员权限。如果是机器人，要求被添加为管理员。
     * - 该接口同样支持批量解除禁言，将[muteEndTimestamp]或[muteSeconds]传值为字符串'0'即可，及需要批量解除禁言的成员的user_id列表[userIds]。
     *
     * @param muteEndTimestamp    string	禁言到期时间戳，绝对时间戳，单位：秒（与[_mute_seconds] 字段同时赋值的话，以该字段为准）
     * @param muteSeconds    string	禁言多少秒（两个字段二选一，默认以 [muteEndTimestamp] 为准）
     * @param userIds    string列表	禁言成员的user_id列表，即[User]的id
     * @return 设置成功的成员user_ids
     * */
    suspend fun setGuildsMuteList(
        guildId: String,
        userIds: List<String>,
        muteEndTimestamp: LocalDateTime?,
        muteSeconds: Duration = Duration.ZERO
    ): List<String>

    // Announces

    /**
     * 创建频道公告
     *
     * 用于创建频道全局公告，公告类型分为 消息类型的频道公告 和 推荐子频道类型的频道公告 。
     *
     * - 当请求参数 message_id 有值时，优先创建消息类型的频道公告， 消息类型的频道公告只能创建成员公告类型的频道公告。
     * - 创建推荐子频道类型的频道全局公告请将 message_id 设置为空，并设置对应的 announces_type 和 recommend_channels 请求参数，
     * 会一次全部替换推荐子频道公司。
     * - 推荐子频道和消息类型全局公告不能同时存在，会互相顶替设置。
     * - 同频道内推荐子频道最多只能创建 3 条。
     * - 只有子频道权限为全体成员可见才可设置为推荐子频道。
     * - 删除推荐子频道类型的频道公告请使用 删除频道公告,并将 message_id 设置为 all。
     *
     * @param messageId    string	选填，消息 id，message_id 有值则优选将某条消息设置为成员公告
     * @param channelId    string	选填，子频道 id，message_id 有值则为必填。
     * @param announcesType    uint32	选填，公告类别 0:成员公告，1:欢迎公告，默认为成员公告
     * @param recommendChannels    RecommendChannel 数组	选填，推荐子频道列表，会一次全部替换推荐子频道列表
     * @return [Announces]
     * */
    suspend fun createGuildsAnnounces(
        guildId: String,
        channelId: String?,
        messageId: String?,
        announcesType: Int = 0,
        recommendChannels: List<RecommendChannel>?
    ): Announces

    /**
     * 删除频道公告
     *
     * 用于删除频道 guild_id 下指定 message_id 的全局公告。
     * - message_id 有值时，会校验 message_id 合法性，若不校验校验 message_id，请将 message_id 设置为 all
     * */
    suspend fun deleteGuildsAnnounces(guildId: String, messageId: String)

    // PinsMessage
    /**
     * 添加精华消息
     *
     * 用于添加子频道 channel_id 内的精华消息。
     * - 精华消息在一个子频道内最多只能创建 20 条。
     * - 只有可见的消息才能被设置为精华消息。
     * - 接口返回对象中 message_ids 为当前请求后子频道内所有精华消息 message_id 数组
     * @return [PinsMessage]
     * */
    suspend fun addChannelsPins(channelId: String, messageId: String): PinsMessage

    /**
     * 删除精华消息
     *
     * 用于删除子频道 channel_id 下指定 message_id 的精华消息。
     * - 删除子频道内全部精华消息，请将 message_id 设置为 all。
     *
     * */
    suspend fun deleteChannelsPins(channelId: String, messageId: String = "all")

    /**
     * 获取精华消息
     *
     * 用于获取子频道 channel_id 内的精华消息。
     *
     * */
    suspend fun getChannelsPins(channelId: String): PinsMessage

    // Schedule
    /**
     * 获取频道日程列表
     *
     * 用于获取channel_id指定的子频道中当天的日程列表。
     * - 若带了参数 since，则返回在 since 对应当天的日程列表；若未带参数 since，则默认返回今天的日程列表。
     *
     * @param since    uint64	起始时间戳(ms)
     * @return [Schedule]数组
     * */
    suspend fun getChannelsSchedules(channelId: String, since: LocalDateTime?): List<Schedule>

    /**
     * 获取日程详情
     *
     * 获取日程子频道 channel_id 下 schedule_id 指定的的日程的详情
     * @return [Schedule]
     * */
    suspend fun getChannelsSchedule(channelId: String, scheduleId: String): Schedule

    /**
     * 创建日程
     *
     * 用于在 channel_id 指定的日程子频道下创建一个日程。
     *
     * - 要求操作人具有管理频道的权限，如果是机器人，则需要将机器人设置为管理员。
     * - 创建成功后，返回创建成功的日程对象。
     * - 创建操作频次限制
     *     - 单个管理员每天限10次。
     *     - 单个频道每天100次。
     *
     * @param schedule    [Schedule]	日程对象，不需要带 id
     * @return [Schedule]
     * */
    suspend fun createChannelsSchedules(channelId: String, schedule: Schedule): Schedule

    /**
     * 修改日程
     *
     * 用于修改日程子频道 channel_id 下 schedule_id 指定的日程的详情。
     *
     * - 要求操作人具有管理频道的权限，如果是机器人，则需要将机器人设置为管理员。
     * */
    suspend fun modifyChannelsSchedules(channelId: String, scheduleId: String, schedule: Schedule)

    /**
     * 删除日程
     *
     * 用于删除日程子频道 channel_id 下 schedule_id 指定的日程。
     * - 要求操作人具有管理频道的权限，如果是机器人，则需要将机器人设置为管理员。
     * */
    suspend fun deleteChannelsSchedules(channelId: String, scheduleId: String)

    // Message Reaction
    /**
     * 发表表情表态
     *
     * @param channelId    string	子频道ID
     * @param messageId    string	消息ID
     * @param emoji    int	表情类型，参考 [EmojiType]
     * */
    suspend fun setChannelsMessagesReactions(channelId: String, messageId: String, emoji: Emoji)

    /**
     * 删除自己的表情表态
     *
     * @param channelId    string	子频道ID
     * @param messageId    string	消息ID
     * @param emoji    int	表情类型，参考 [EmojiType]
     * */
    suspend fun deleteChannelsMessagesReactions(channelId: String, messageId: String, emoji: Emoji)

    /**
     * 拉取表情表态用户列表
     *
     * 拉取对消息 message_id 指定表情表态的用户列表
     * @param channelId    string	子频道ID
     * @param messageId    string	消息ID
     * @param emoji    int	表情类型，参考 [EmojiType]
     * @param cookie    string	上次请求返回的cookie，第一次请求无需填写
     * @param limit    int	每次拉取数量，默认20，最多50，只在第一次请求时设置
     * @return [ReactionList]
     * */
    suspend fun getChannelsMessagesReactions(
        channelId: String,
        messageId: String,
        emoji: Emoji,
        cookie: String? = null,
        limit: Int = 20
    ): ReactionList

    // Audio Control

    /**
     * 音频控制
     *
     * 用于控制子频道 channel_id 下的音频。
     * - 音频接口：仅限音频类机器人才能使用，后续会根据机器人类型自动开通接口权限，现如需调用，需联系平台申请权限。
     * @param channelId
     * @param audioControl [AudioControl]
     * */
    suspend fun setChannelsAudio(channelId: String, audioControl: AudioControl)

    /**
     * 机器人上麦
     *
     * 机器人在 channelId 对应的语音子频道上麦。
     * - 音频接口：仅限音频类机器人才能使用，后续会根据机器人类型自动开通接口权限，现如需调用，需联系平台申请权限。
     * @param channelId
     * */
    suspend fun putChannelsMic(channelId: String)

    /**
     * 机器人下麦
     *
     * 机器人在 channelId 对应的语音子频道下麦。
     * - 音频接口：仅限音频类机器人才能使用，后续会根据机器人类型自动开通接口权限，现如需调用，需联系平台申请权限。
     * @param channelId
     * */
    suspend fun deleteChannelsMic(channelId: String)

    // forum

    /**
     * 获取帖子列表
     *
     * 该接口用于获取子频道下的帖子列表
     * @param channelId
     * @return threads	Thread	帖子列表对象（返回值里面的content字段，可参照RichText结构）
     * is_finish	uint32	是否拉取完毕(0:否；1:是)
     * */
    suspend fun getChannelsThreads(channelId: String): ThreadsResp

    /**
     * 获取帖子详情
     *
     * 该接口用于获取子频道下的帖子详情
     * */
    suspend fun getChannelsThread(channelId: String, threadId: String): ThreadResp

    /**
     * 发表帖子
     *
     * 创建成功后，返回创建成功的任务ID。
     *
     * */
    suspend fun createChannelsThreads(channelId: String, title: String, content: String, format: Format): CreateThreadResp

    /**
     *
     * */
    suspend fun deleteChannelsThreads(channelId: String, threadId: String)
    // API Permissions
    /**
     * 获取频道可用权限列表
     *
     * 获取频道可用权限列表
     * @return [APIPermission]数组
     * */
    suspend fun getGuildsApiPermission(guildId: String): List<APIPermission>

    /**
     * 创建频道 API 接口权限授权链接
     *
     * 用于创建 API 接口权限授权链接，该链接指向guild_id对应的频道 。
     * 需要注意，私信场景中，当需要查询私信来源频道的权限时，应使用src_guild_id，即 [Message]中的src_guild_id
     * - 每天只能在一个频道内发 3 条（默认值）频道权限授权链接。
     * @param channelId    string	授权链接发送的子频道 id
     * @param apiIdentify    [APIPermissionDemandIdentify] 对象	api 权限需求标识对象
     * @param desc    string	机器人申请对应的 API 接口权限后可以使用功能的描述
     * @return [APIPermissionDemand]
     * */
    suspend fun createGuildsApiPermissionDemand(
        guildId: String,
        channelId: String,
        apiIdentify: APIPermissionDemandIdentify,
        desc: String
    ): APIPermissionDemand

    // ws API
    /**
     * 获取通用 WSS 接入点
     *
     * 用于获取 WSS 接入地址，通过该地址可建立 websocket 长连接。
     * */
    suspend fun getGateway(): String

    /**
     * 获取带分片 WSS 接入点
     *
     * 用于获取 WSS 接入地址及相关信息，通过该地址可建立 websocket 长连接。相关信息包括：
     *
     * - 建议的分片数。
     * - 目前连接数使用情况。
     * @return [WebsocketApi]
     * */
    suspend fun getGatewayBot(): WebsocketApi

    /**
     *
     * */
    suspend fun postUsersMessage(): Message

    /**
     *
     * */
    suspend fun postGroupsMessage(): Message
}
