package ng.i.sav.qdroid.infra.model

import com.fasterxml.jackson.annotation.JsonProperty
import ng.i.sav.qdroid.infra.client.ApiRequest
import java.time.LocalDateTime

/**
 * @property id    string	消息 id
 * @property channelId    string	子频道 id
 * @property guildId    string	频道 id
 * @property content    string	消息内容
 * @property timestamp    ISO8601 timestamp	消息创建时间
 * @property editedTimestamp    ISO8601 timestamp	消息编辑时间
 * @property mentionEveryone    bool	是否是@全员消息
 * @property author    User 对象	消息创建者
 * @property attachments    [MessageAttachment] 对象数组	附件
 * @property embeds    [MessageEmbed] 对象数组	embed
 * @property mentions    [User] 对象数组	消息中@的人
 * @property member    [Member] 对象	消息创建者的member信息
 * @property ark    [MessageArk] ark消息对象	ark消息
 * @property seq    int	用于消息间的排序，seq 在同一子频道中按从先到后的顺序递增，不同的子频道之间消息无法排序。(目前只在消息事件中有值，2022年8月1日 后续废弃)
 * @property seqInChannel    string	子频道消息 seq，用于消息间的排序，seq 在同一子频道中按从先到后的顺序递增，不同的子频道之间消息无法排序
 * @property messageReference    [MessageReference] 对象	引用消息对象
 * @property srcGuildId    string	用于私信场景下识别真实的来源频道id
 * */
data class Message(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("channel_id")
    val channelId: String,
    @JsonProperty("guild_id")
    val guildId: String,
    @JsonProperty("content")
    val content: String?,
    @JsonProperty("timestamp")
    val timestamp: LocalDateTime,
    @JsonProperty("edited_timestamp")
    val editedTimestamp: LocalDateTime?,
    @JsonProperty("mention_everyone")
    val mentionEveryone: Boolean,
    @JsonProperty("author")
    val author: User,
    @JsonProperty("attachments")
    val attachments: List<MessageAttachment>?,
    @JsonProperty("embeds")
    val embeds: List<MessageEmbed>?,
    @JsonProperty("mentions")
    val mentions: List<User>?,
    @JsonProperty("member")
    val member: Member?,
    @JsonProperty("ark")
    val ark: MessageArk?,
    @JsonProperty("seq")
    val seq: Int,
    @JsonProperty("seq_in_channel")
    val seqInChannel: String,
    @JsonProperty("message_reference")
    val messageReference: MessageReference?,
    @JsonProperty("src_guild_id")
    val srcGuildId: String?,
)

suspend fun Message.sendText(bot: ApiRequest, content: String) {
    bot.postChannelsMessages(
        channelId, msgId = id, content = content
    )
}

/**
 * @property title    string	标题
 * @property  prompt    string	消息弹窗内容
 * @property thumbnail    [MessageEmbedThumbnail] 对象	缩略图
 * @property fields    [MessageEmbedField] 对象数组	embed 字段数据
 * */
data class MessageEmbed(
    val title: String,
    val prompt: String,
    val thumbnail: MessageEmbedThumbnail,
    val fields: MessageEmbedField,
)

/**
 * @property url    string	图片地址
 * */
data class MessageEmbedThumbnail(val url: String)

/**
 * @property name    string	字段名
 * */
data class MessageEmbedField(val name: String)

/**
 * @property url    string	下载地址
 * */
data class MessageAttachment(val url: String)

/**
 * @property templateId    int	ark模板id（需要先申请）
 * @property kv    [MessageArkKv] ark kv数组	kv值列表
 * */
data class MessageArk(
    @JsonProperty("template_id")
    val templateId: Int,
    @JsonProperty("kv")
    val kv: List<MessageArkKv>
)

/**
 * @property key    string	key
 * @property value    string	value
 * @property obj    [MessageArkObj] ark obj类型的数组	ark obj类型的列表
 *
 */
data class MessageArkKv(
    @JsonProperty("key")
    val key: String,
    @JsonProperty("value")
    val value: String,
    @JsonProperty("obj")
    val obj: MessageArkObj
)

/**
 * @property objKv    [MessageArkObjKv] obj kv类型的数组	ark obj kv列表
 * */
data class MessageArkObj(@JsonProperty("obj_kv") val objKv: List<MessageArkObjKv>)

/**
 * @property key    string	key
 * @property value    string	value
 * */
data class MessageArkObjKv(
    @JsonProperty("key")
    val key: String,
    @JsonProperty("value")
    val value: String
)

/**
 * @property messageId    string	需要引用回复的消息 id
 * @property ignoreGetMessageError    bool	是否忽略获取引用消息详情错误，默认否
 * */

data class MessageReference(
    @JsonProperty("message_id")
    val messageId: String,
    @JsonProperty("ignore_get_message_error")
    val ignoreGetMessageError: Boolean = false
)

/**
 * @property templateId    int	markdown 模板 id
 * @property customTemplateId    string	markdown 自定义模板 id
 * @property params    [MessageMarkdownParams]	markdown 模板模板参数
 * @property content    string	原生 markdown 内容,与上面三个参数互斥,参数都传值将报错。
 * */
data class MessageMarkdown(
    @JsonProperty("template_id")
    val templateId: Int? = null,
    @JsonProperty("custom_template_id")
    val customTemplateId: String? = null,
    @JsonProperty("params")
    val params: MessageMarkdownParams? = null,
    @JsonProperty("content")
    val content: String? = null
)

/**
 * @property key    string	markdown 模版 key
 * @property values    string 类型的数组	markdown 模版 key 对应的 values ，列表长度大小为 1，传入多个会报错
 * */
data class MessageMarkdownParams(
    @JsonProperty("key")
    val key: String,
    @JsonProperty("values")
    val values: List<String>
) {
    constructor(key: String, value: String) : this(key, listOf(value))
}

/**
 * @property message    [Message] 对象	被删除的消息内容
 * @property opUser    [User] 对象	执行删除操作的用户
 * */
data class MessageDelete(
    @JsonProperty("message")
    val message: Message,
    @JsonProperty("op_user")
    val opUser: User
)

/**
 * @property id    string	keyboard 模板 id
 * @property content    [InlineKeyboard] 对象	自定义 keyboard 内容,与 id 参数互斥,参数都传值将报错。
 * */
data class MessageKeyboard(
    @JsonProperty("id")
    val id: String? = null,
    @JsonProperty("content")
    val content: InlineKeyboard? = null
)

/**
 * @property auditId    string	消息审核 id
 * @property messageId    string	消息 id，只有审核通过事件才会有值
 * @property guildId    string	频道 id
 * @property channelId    string	子频道 id
 * @property auditTime    ISO8601 timestamp	消息审核时间
 * @property createTime    ISO8601 timestamp	消息创建时间
 * @property seqInChannel    string	子频道消息 seq，用于消息间的排序，seq 在同一子频道中按从先到后的顺序递增，不同的子频道之间消息无法排序
 * */
data class MessageAudited(
    @JsonProperty("audit_id")
    val auditId: String,
    @JsonProperty("message_id")
    val messageId: String,
    @JsonProperty("guild_id")
    val guildId: String,
    @JsonProperty("channel_id")
    val channelId: String,
    @JsonProperty("audit_time")
    val auditTime: LocalDateTime,
    @JsonProperty("create_time")
    val createTime: LocalDateTime,
    @JsonProperty("seq_in_channel")
    val seqInChannel: String
)

/**
 * *@用户*    <@user_id> 或者 <@!user_id>	解析为 @用户 标签	<@1234000000001>
 * *@所有人*    @everyone	解析为 @所有人 标签，需要机器人拥有发送 @所有人 消息的权限	@everyone
 * #子频道	<#channel_id>	解析为 #子频道 标签，点击可以跳转至子频道，仅支持当前频道内的子频道	<#12345>
 * 表情	<emoji:id>	解析为系统表情，具体表情id参考 Emoji 列表，仅支持type=1的系统表情，type=2的emoji表情直接按字符串填写即可	<emoji:4> 解析为得意表情
 * #转义内容
 * 消息抄送会将源字符转为转义后内容然后抄送给机器人
 * 发消息会将转义后字符转为源字符后抄再发
 *
 * |源字符|转义后|
 * |:---:|:---:|
 * |  &	| &amp; |
 * |  < | &lt; |
 * | \> | &gt;|
 * */
object EmbedContent {
    fun atUser(userId: String): String {
        return "<qqbot-at-user id=\"$userId\" />"
    }

    fun atAll(): String {
        return "<qqbot-at-everyone />"
    }

    fun channel(channelId: String): String {
        return "<#$channelId>"
    }

    fun emoji(emojiId: Int): String {
        return "<emoji:$emojiId>"
    }
}
