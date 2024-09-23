package ng.i.sav.bot.qdroid.model

import ng.i.sav.bot.qdroid.model.Alignment.*
import ng.i.sav.bot.qdroid.model.AtType.*
import ng.i.sav.bot.qdroid.model.AuditType.*
import ng.i.sav.bot.qdroid.model.ElemType.*
import ng.i.sav.bot.qdroid.model.RichType.*
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import java.time.LocalDateTime

/**
 * @property guildId    string	频道ID
 * @property channelId    string	子频道ID
 * @property authorId    string	作者ID
 * @property threadInfo    [ThreadInfo]	主帖内容
 * */
data class Thread(
    @JsonProperty("guild_id") val guildId: String,
    @JsonProperty("channel_id") val channelId: String,
    @JsonProperty("author_id") val authorId: String,
    @JsonProperty("thread_info") val threadInfo: ThreadInfo
)

/**
 * @property threadId    string	主帖ID
 * @property title    string	帖子标题
 * @property content    string	帖子内容
 * @property dateTime    String	发表时间 (ISO8601 timestamp)
 * */
data class ThreadInfo(
    @JsonProperty("thread_id") val threadId: String,
    @JsonProperty("title") val title: String,
    @JsonProperty("content") val content: String,
    @JsonProperty("date_time") val dateTime: LocalDateTime
)

/**
 * 话题频道内对主题的评论称为帖子
 * 话题频道内对帖子主题评论或删除时生产事件中包含该对象
 * @property guildId    string	频道ID
 * @property channelId    string	子频道ID
 * @property authorId    string	作者ID
 * @property postInfo    [PostInfo]	帖子内容
 */
data class Post(
    @JsonProperty("guild_id") val guildId: String,
    @JsonProperty("channel_id") val channelId: String,
    @JsonProperty("author_id") val authorId: String,
    @JsonProperty("post_info") val postInfo: PostInfo
)

/**
 * 帖子事件包含的帖子内容信息
 * @property threadId    string	主题ID
 * @property postId    string	帖子ID
 * @property content    string	帖子内容
 * @property dateTime    string	评论时间
 */
data class PostInfo(
    @JsonProperty("thread_id") val threadId: String,
    @JsonProperty("post_id") val postId: String,
    @JsonProperty("content") val content: String,
    @JsonProperty("date_time") val dateTime: LocalDateTime
)

/**
 * 话题频道对帖子回复或删除时生产该事件中包含该对象
 * 话题频道内对帖子的评论称为回复
 * @property guildId    String    频道ID
 * @property channelId    String    子频道ID
 * @property authorId    String    作者ID
 * @property replyInfo    [ReplyInfo]    回复内容
 */
data class Reply(
    @JsonProperty("guild_id") val guildId: String,
    @JsonProperty("channel_id") val channelId: String,
    @JsonProperty("author_id") val authorId: String,
    @JsonProperty("reply_info") val replyInfo: ReplyInfo
)

/**
 * 回复事件包含的回复内容信息
 * @property threadId    String    主题ID
 * @property postId    String    帖子ID
 * @property replyId    String    回复ID
 * @property content    String    回复内容
 * @property dateTime    String    回复时间
 */
data class ReplyInfo(
    @JsonProperty("thread_id") val threadId: String,
    @JsonProperty("post_id") val postId: String,
    @JsonProperty("reply_id") val replyId: String,
    @JsonProperty("content") val content: String,
    @JsonProperty("date_time") val dateTime: LocalDateTime
)

/**
 * 论坛帖子审核结果事件
 * @property guildId    String    频道ID
 * @property channelId    String    子频道ID
 * @property authorId    String    作者ID
 * @property threadId    String    主题ID
 * @property postId    String    帖子ID
 * @property replyId    String    回复ID
 * @property type    uint32    [AuditType]审核的类型
 * @property result    uint32    审核结果. 0:成功 1:失败
 * @property errMsg    String    result不为0时错误信息
 */
data class AuditResult(
    @JsonProperty("guild_id") val guildId: String,
    @JsonProperty("channel_id") val channelId: String,
    @JsonProperty("author_id") val authorId: String,
    @JsonProperty("thread_id") val threadId: String,
    @JsonProperty("post_id") val postId: String,
    @JsonProperty("reply_id") val replyId: String,
    @JsonProperty("type") val type: AuditType,
    @JsonProperty("result") val result: Int,
    @JsonProperty("err_msg") val errMsg: String?
)

/**
 * @property PUBLISH_THREAD 1 帖子
 * @property PUBLISH_POST 2 评论
 * @property PUBLISH_REPLY 3 回复
 */
@JsonDeserialize(using = AuditType.Deserializer::class)
@JsonSerialize(using = AuditType.Serializer::class)
enum class AuditType(
    val value: Int,
    val desc: String,
) {
    PUBLISH_THREAD(1, "帖子"), PUBLISH_POST(2, "评论"), PUBLISH_REPLY(3, "回复"), UNKNOWN(-1, "未知类型");

    class Deserializer : JsonDeserializer<AuditType>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): AuditType {
            val i = p.valueAsInt
            return entries.find { it.value == i } ?: UNKNOWN
        }
    }

    class Serializer : JsonSerializer<AuditType>() {
        override fun serialize(value: AuditType, gen: JsonGenerator, serializers: SerializerProvider?) {
            gen.writeNumber(value.value)
        }
    }
}

/**
 * 富文本内容
 * @property type    Int    [RichType]    富文本类型
 * @property textInfo    [TextInfo]    文本
 * @property atInfo    [AtInfo]    @ 内容
 * @property urlInfo    [URLInfo]    链接
 * @property emojiInfo    [EmojiInfo]    表情
 * @property channelInfo    [ChannelInfo]    提到的子频道
 */
data class RichObject(
    val type: RichType,
    val textInfo: TextInfo,
    val atInfo: AtInfo,
    val urlInfo: URLInfo,
    val emojiInfo: EmojiInfo,
    val channelInfo: ChannelInfo
)

/**
 * @property TEXT 1 普通文本
 * @property AT 2 at信息
 * @property URL 3 url信息
 * @property EMOJI 4 表情
 * @property CHANNEL 5 #子频道
 * @property VIDEO 10 视频
 * @property IMAGE 11 图片
 */
@JsonDeserialize(using = RichType.Deserializer::class)
@JsonSerialize(using = RichType.Serializer::class)
enum class RichType(
    val value: Int,
    val desc: String,
) {
    TEXT(1, "普通文本"), AT(2, "at信息"), URL(3, "url信息"), EMOJI(4, "表情"), CHANNEL(5, "#子频道"), VIDEO(
        10,
        "视频"
    ),
    IMAGE(11, "图片"), UNKNOWN(-1, "未知类型");

    class Deserializer : JsonDeserializer<RichType>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): RichType {
            val i = p.valueAsInt
            return entries.find { it.value == i } ?: UNKNOWN
        }
    }

    class Serializer : JsonSerializer<RichType>() {
        override fun serialize(value: RichType, gen: JsonGenerator, serializers: SerializerProvider?) {
            gen.writeNumber(value.value)
        }
    }
}

/**
 * 富文本 - 普通文本
 * @property text    String    普通文本
 */
data class TextInfo(
    @JsonProperty("text") val text: String
)

/**
 * 富文本 - @内容
 * @property type    [AtType]    at类型
 * @property userInfo    [AtUserInfo]    用户
 * @property roleInfo    [AtRoleInfo]    角色组信息
 * @property guildInfo    [AtGuildInfo]    频道信息
 */
data class AtInfo(
    @JsonProperty("type") val type: AtType,
    @JsonProperty("user_info") val userInfo: AtUserInfo,
    @JsonProperty("role_info") val roleInfo: AtRoleInfo,
    @JsonProperty("guild_info") val guildInfo: AtGuildInfo
)

/**
 * @property AT_EXPLICIT_USER 1 at特定人
 * @property AT_ROLE_GROUP 2 at角色组所有人
 * @property AT_GUILD 3 at频道所有人
 */
@JsonDeserialize(using = AtType.Deserializer::class)
@JsonSerialize(using = AtType.Serializer::class)
enum class AtType(
    val value: Int,
    val desc: String,
) {
    AT_EXPLICIT_USER(1, "at特定人"), AT_ROLE_GROUP(2, "at角色组所有人"), AT_GUILD(3, "at频道所有人"), UNKNOWN(
        -1,
        "未知类型"
    );

    class Deserializer : JsonDeserializer<AtType>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): AtType {
            val i = p.valueAsInt
            return entries.find { it.value == i } ?: UNKNOWN
        }
    }

    class Serializer : JsonSerializer<AtType>() {
        override fun serialize(value: AtType, gen: JsonGenerator, serializers: SerializerProvider?) {
            gen.writeNumber(value.value)
        }
    }
}

/**
 * *@用户信息*
 * @property id    String    身份组ID
 * @property nick    String    用户昵称
 */
data class AtUserInfo(
    @JsonProperty("id") val id: String, @JsonProperty("nick") val nick: String
)

/**
 * *@身份组信息*
 * @property roleId    Long    身份组ID
 * @property name    String    身份组名称
 * @property color    uint32颜色值
 */
data class AtRoleInfo(
    @JsonProperty("role_id") val roleId: Long,
    @JsonProperty("name") val name: String,
    @JsonProperty("color") val color: UInt
)

/**
 * *@频道信息*
 * @property guildId    String    频道ID
 * @property guildName    String    频道名称
 */
data class AtGuildInfo(
    @JsonProperty("guild_id") val guildId: String, @JsonProperty("guild_name") val guildName: String
)

/**
 * 富文本 - 链接信息
 * @property url    String    链接地址
 * @property displayText    String    链接显示文本
 */
data class URLInfo(
    @JsonProperty("url") val url: String, @JsonProperty("display_text") val displayText: String
)

/**
 * 富文本 - Emoji信息
 * @property id    String    表情id
 * @property type    String    表情类型
 * @property name    String    名称
 * @property url    String    链接
 */
data class EmojiInfo(
    @JsonProperty("id") val id: String,
    @JsonProperty("type") val type: String,
    @JsonProperty("name") val name: String,
    @JsonProperty("url") val url: String
)

/**
 * 富文本 - 子频道信息
 * @property channelId    Long    子频道id
 * @property channelName    String    子频道名称
 */
data class ChannelInfo(
    @JsonProperty("channel_id") val channelId: Long, @JsonProperty("channel_name") val channelName: String
)

/**
 * 富文本内容
 * @property paragraphs    List<[Paragraph]>    段落，一段落一行，段落内无元素的为空行
 */
data class RichText(
    @JsonProperty("paragraphs") val paragraphs: List<Paragraph>
)

/**
 * 富文本 - 段落结构
 * @property elems    List<[Elem]>    元素列表
 * @property props    [ParagraphProps]    段落属性
 */
data class Paragraph(
    @JsonProperty("elems") val elems: List<Elem>, @JsonProperty("props") val props: ParagraphProps
)

/**
 * 富文本 - 元素列表结构
 * @property text    [TextElem]    文本元素
 * @property image    [ImageElem]    图片元素
 * @property video    [VideoElem]    视频元素
 * @property url    [URLElem]    URL元素
 * @property type    [ElemType]    元素类型
 */
data class Elem(
    @JsonProperty("text") val text: TextElem,
    @JsonProperty("image") val image: ImageElem,
    @JsonProperty("video") val video: VideoElem,
    @JsonProperty("url") val url: URLElem,
    @JsonProperty("type") val type: ElemType
)

/**
 * @property ELEM_TYPE_TEXT 1 文本
 * @property ELEM_TYPE_IMAGE 2 图片
 * @property ELEM_TYPE_VIDEO 3 视频
 * @property ELEM_TYPE_URL 4 URL
 */
@JsonDeserialize(using = ElemType.Deserializer::class)
@JsonSerialize(using = ElemType.Serializer::class)
enum class ElemType(
    val value: Int, val description: String
) {
    ELEM_TYPE_TEXT(1, "文本"), ELEM_TYPE_IMAGE(2, "图片"), ELEM_TYPE_VIDEO(3, "视频"), ELEM_TYPE_URL(4, "URL"), UNKNOWN(
        -1,
        "未知类型"
    );

    class Deserializer : JsonDeserializer<ElemType>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ElemType {
            val i = p.valueAsInt
            return ElemType.values().find { it.value == i } ?: UNKNOWN
        }
    }

    class Serializer : JsonSerializer<ElemType>() {
        override fun serialize(value: ElemType, gen: JsonGenerator, serializers: SerializerProvider?) {
            gen.writeNumber(value.value)
        }
    }
}

/**
 * 富文本 - 文本属性
 * @property text    String    正文
 * @property props    TextProps    文本属性
 */
data class TextElem(
    @JsonProperty("text") val text: String, @JsonProperty("props") val props: TextProps
)

/**
 * 富文本 - 图片属性
 * @property thirdUrl    String    第三方图片链接
 * @property widthPercent    Double    宽度比例（缩放比，在屏幕里显示的比例）
 */
data class ImageElem(
    @JsonProperty("third_url") val thirdUrl: String, @JsonProperty("width_percent") val widthPercent: Double
)

/**
 * 富文本 - 视频属性
 * @property thirdUrl    String    第三方视频文件链接
 */
data class VideoElem(
    @JsonProperty("third_url") val thirdUrl: String
)

/**
 * 富文本 - URL属性
 * @property url    String    URL链接
 * @property desc    String    URL描述
 */
data class URLElem(
    @JsonProperty("url") val url: String, @JsonProperty("desc") val desc: String
)

/**
 * 富文本 - 文本段落属性
 * @property fontBold    Boolean    加粗
 * @property italic    Boolean    斜体
 * @property underline    Boolean    下划线
 */
data class TextProps(
    @JsonProperty("font_bold") val fontBold: Boolean,
    @JsonProperty("italic") val italic: Boolean,
    @JsonProperty("underline") val underline: Boolean
)

/**
 * 富文本 - 平台图片属性
 * @property url    String    架平图片链接
 * @property width    UInt    图片宽度
 * @property height    UInt    图片高度
 * @property imageId    String    图片ID
 */
data class PlatImage(
    @JsonProperty("url") val url: String,
    @JsonProperty("width") val width: Int,
    @JsonProperty("height") val height: Int,
    @JsonProperty("image_id") val imageId: String
)

/**
 * 富文本 - 平台视频属性
 * @property url    String    架平图片链接
 * @property width    UInt    图片宽度
 * @property height    UInt    图片高度
 * @property videoId    String    视频ID
 * @property duration    UInt    视频时长
 * @property cover    PlatImage    视频封面图属性
 */
data class PlatVideo(
    @JsonProperty("url") val url: String,
    @JsonProperty("width") val width: UInt,
    @JsonProperty("height") val height: UInt,
    @JsonProperty("video_id") val videoId: String,
    @JsonProperty("duration") val duration: Int,
    @JsonProperty("cover") val cover: PlatImage
)

/**
 * 富文本 - 段落属性
 * @property alignment    Int    段落对齐方向属性，数值可以参考 [Alignment]
 */
data class ParagraphProps(
    @JsonProperty("alignment") val alignment: Alignment
)

/**
 * @property ALIGNMENT_LEFT 0 左对齐
 * @property ALIGNMENT_MIDDLE 1 居中
 * @property ALIGNMENT_RIGHT 2 右对齐
 */
@JsonDeserialize(using = Alignment.Deserializer::class)
@JsonSerialize(using = Alignment.Serializer::class)
enum class Alignment(
    val value: Int,
    val desc: String,
) {
    ALIGNMENT_LEFT(0, "左对齐"), ALIGNMENT_MIDDLE(1, "居中"), ALIGNMENT_RIGHT(2, "右对齐"), UNKNOWN(-1, "未知对齐方式");

    class Deserializer : JsonDeserializer<Alignment>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Alignment {
            val i = p.valueAsInt
            return entries.find { it.value == i } ?: UNKNOWN
        }

    }

    class Serializer : JsonSerializer<Alignment>() {
        override fun serialize(value: Alignment, gen: JsonGenerator, serializers: SerializerProvider?) {
            gen.writeNumber(value.value)
        }
    }
}

/**
 * @property threads 帖子列表对象（返回值里面的content字段，可参照[RichText]结构）
 * @property isFinish 是否拉取完毕(0:否；1:是)
 * */
data class ThreadsResp(
    @JsonProperty("threads") val threads: List<Thread>, @JsonProperty("is_finish") val isFinish: Int
)

/**
 * @property thread [ThreadInfo] 帖子详情对象（返回值里面的content字段，可参照RichText结构）
 * */
data class ThreadResp(val thread: ThreadInfo)

/**
 * @property TEXT 1 普通文本
 * @property HTML 2 HTML
 * @property MARKDOWN 3 Markdown
 * @property JSON 4 JSON（content参数可参照[RichText]结构）
 * */
@JsonDeserialize(using = Format.Deserializer::class)
@JsonSerialize(using = Format.Serializer::class)
enum class Format(
    val value: Int,
    val desc: String,
) {
    TEXT(1, "普通文本"),
    HTML(2, "HTML"),
    MARKDOWN(3, "Markdown"),
    JSON(4, "JSON（content参数可参照RichText结构）"),
    UNKNOWN(-1, "未知格式");

    class Deserializer : JsonDeserializer<Format>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Format {
            val i = p.valueAsInt
            return entries.find { it.value == i } ?: UNKNOWN
        }
    }

    class Serializer : JsonSerializer<Format>() {
        override fun serialize(value: Format, gen: JsonGenerator, serializers: SerializerProvider?) {
            gen.writeNumber(value.value)
        }
    }
}

/**
 * @property taskId    string    帖子任务ID
 * @property createTime    string    发帖时间戳，单位：秒
 */
data class CreateThreadResp(
    @JsonProperty("task_id")
    val taskId: String,
    @JsonProperty("create_time")
    val createTime: String
)
