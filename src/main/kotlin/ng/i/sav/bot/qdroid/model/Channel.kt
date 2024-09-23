package ng.i.sav.bot.qdroid.model

import ng.i.sav.bot.qdroid.model.PrivateType.*
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize

/**
 * @property id    String	子频道 id
 * @property guildId    string	频道 id
 * @property name    string	子频道名
 * @property type    int	子频道类型 [ChannelType]
 * @property subType    int	子频道子类型 [ChannelSubType]
 * @property position    int	排序值，具体请参考 有关 position 的说明
 * @property parentId    string	所属分组 id，仅对子频道有效，对 子频道分组（[ChannelType]=4） 无效
 * @property ownerId    string	创建人 id
 * @property privateType    int	子频道私密类型 [PrivateType]
 * @property speakPermission    int	子频道发言权限 [SpeakPermission]
 * @property applicationId    string	用于标识应用子频道应用类型，仅应用子频道时会使用该字段，具体定义请参考 应用子频道的应用类型
 * @property permissions    string	用户拥有的子频道权限 [Permissions]
 * */
data class ChannelDetail(
    val id: String,
    @JsonProperty("guild_id")
    val guildId: String,
    val name: String,
    val type: ChannelType,
    @JsonProperty("sub_type")
    val subType: Int?,
    val position: Int?,
    @JsonProperty("parent_id")
    val parentId: String,
    @JsonProperty("owner_id")
    val ownerId: String,
    @JsonProperty("private_type")
    val privateType: Int?,
    @JsonProperty("speak_permission")
    val speakPermission: Int?,
    @JsonProperty("application_id")
    val applicationId: String?,
    val permissions: String?
)

/**
 * @property id    String	子频道 id
 * @property guildId    string	频道 id
 * @property name    string	子频道名
 * @property type    int	子频道类型 [ChannelType]
 * @property subType    int	子频道子类型 [ChannelSubType]
 * @property position    int	排序值，具体请参考 有关 position 的说明
 * @property parentId    string	所属分组 id，仅对子频道有效，对 子频道分组（[ChannelType]=4） 无效
 * @property ownerId    string	创建人 id
 * @property privateType    int	子频道私密类型 [PrivateType]
 * */
data class Channel(
    val id: String,
    @JsonProperty("guild_id")
    val guildId: String,
    val name: String,
    val type: ChannelType,
    @JsonProperty("sub_type")
    val subType: Int?,
    val position: Int?,
    @JsonProperty("parent_id")
    val parentId: String,
    @JsonProperty("owner_id")
    val ownerId: String,
    @JsonProperty("private_type")
    val privateType: Int?
)

/**
 * 0	文字子频道
 * 1	保留，不可用
 * 2	语音子频道
 * 3	保留，不可用
 * 4	子频道分组
 * 10005	直播子频道
 * 10006	应用子频道
 * 10007	论坛子频道
 * */
@JsonDeserialize(using = ChannelType.Deserializer::class)
@JsonSerialize(using = ChannelType.Serializer::class)
enum class ChannelType(
    val type: Int,
    val typeName: String,
) {
    TEXT(0, "文字子频道"),
    AUDIO(2, "语音子频道"),
    GROUP(4, "子频道分组"),
    LIVE(10005, "直播子频道"),
    APPLICATION(10006, "应用子频道"),
    BBS(10007, "论坛子频道"),
    UNKNOWN(-1, "未知类型");

    class Deserializer : JsonDeserializer<ChannelType>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ChannelType {
            val i = p.valueAsInt
            return entries.find { it.type == i } ?: UNKNOWN
        }

    }

    class Serializer : JsonSerializer<ChannelType>() {
        override fun serialize(value: ChannelType, gen: JsonGenerator, serializers: SerializerProvider?) {
            gen.writeNumber(value.type)
        }
    }
}

/**
 * 0	闲聊
 * 1	公告
 * 2	攻略
 * 3	开黑
 * */
@JsonDeserialize(using = ChannelSubType.Deserializer::class)
@JsonSerialize(using = ChannelSubType.Serializer::class)
enum class ChannelSubType(
    val type: Int,
    val typeName: String,
) {
    CHAT(0, "闲聊"),
    ANNOUNCE(1, "公告"),
    STRATEGY(2, "攻略"),
    TOGETHER(3, "开黑"),
    UNKNOWN(-1, "未知类型");

    class Deserializer : JsonDeserializer<ChannelSubType>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ChannelSubType {
            val i = p.valueAsInt
            return ChannelSubType.entries.find { it.type == i } ?: UNKNOWN
        }

    }

    class Serializer : JsonSerializer<ChannelSubType>() {
        override fun serialize(value: ChannelSubType, gen: JsonGenerator, serializers: SerializerProvider?) {
            gen.writeNumber(value.type)
        }
    }
}

/**
 * @property PUBLIC 0	公开频道
 * @property PRIVATE 1	群主管理员可见
 * @property PROTECTED 2	群主管理员+指定成员，可使用 修改子频道权限接口 指定成员
 * */
@JsonDeserialize(using = PrivateType.Deserializer::class)
@JsonSerialize(using = PrivateType.Serializer::class)
enum class PrivateType(
    val type: Int,
    val typeName: String,
) {
    PUBLIC(0, "公开频道"),
    PRIVATE(1, "群主管理员可见"),
    PROTECTED(2, "群主管理员+指定成员"),
    UNKNOWN(-1, "未知类型");

    class Deserializer : JsonDeserializer<PrivateType>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): PrivateType {
            val i = p.valueAsInt
            return PrivateType.entries.find { it.type == i } ?: UNKNOWN
        }

    }

    class Serializer : JsonSerializer<PrivateType>() {
        override fun serialize(value: PrivateType, gen: JsonGenerator, serializers: SerializerProvider?) {
            gen.writeNumber(value.type)
        }
    }
}

/**
 * 0	无效类型
 * 1	所有人
 * 2	群主管理员+指定成员，可使用 修改子频道权限接口 指定成员
 *  */
@JsonDeserialize(using = SpeakPermission.Deserializer::class)
@JsonSerialize(using = SpeakPermission.Serializer::class)
enum class SpeakPermission(
    val type: Int,
    val typeName: String,
) {
    INVALID(0, "无效类型"),
    ALL(1, ""),
    PROTECTED(2, "群主管理员+指定成员"),
    UNKNOWN(-1, "未知类型");

    class Deserializer : JsonDeserializer<SpeakPermission>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): SpeakPermission {
            val i = p.valueAsInt
            return SpeakPermission.entries.find { it.type == i } ?: UNKNOWN
        }

    }

    class Serializer : JsonSerializer<SpeakPermission>() {
        override fun serialize(value: SpeakPermission, gen: JsonGenerator, serializers: SerializerProvider?) {
            gen.writeNumber(value.type)
        }
    }
}

/**
 * 1000000	王者开黑大厅
 * 1000001	互动小游戏
 * 1000010	腾讯投票
 * 1000051	飞车开黑大厅
 * 1000050	日程提醒
 * 1000070	CoDM 开黑大厅
 * 1010000	和平精英开黑大厅
 * */
@JsonDeserialize(using = ApplicationId.Deserializer::class)
@JsonSerialize(using = ApplicationId.Serializer::class)
enum class ApplicationId(
    val type: String,
    val typeName: String,
) {
    WANGZHE("1000000", "王者开黑大厅"),
    HUDONG("1000001", "互动小游戏"),
    POOL("1000010", "腾讯投票"),
    FEICHE("飞车开黑大厅", "飞车开黑大厅"),
    REMINDER("1000050", "日程提醒"),
    CODM("1000070", "CoDM 开黑大厅"),
    HEPINJINGYING("1010000", "和平精英开黑大厅"),
    UNKNOWN("-1", "未知类型");

    class Deserializer : JsonDeserializer<ApplicationId>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ApplicationId {
            val i = p.valueAsString
            return ApplicationId.entries.find { it.type == i } ?: UNKNOWN
        }

    }

    class Serializer : JsonSerializer<ApplicationId>() {
        override fun serialize(value: ApplicationId, gen: JsonGenerator, serializers: SerializerProvider?) {
            gen.writeNumber(value.type)
        }
    }
}

/**
 * @property name    string	子频道名称
 * @property type    int	子频道类型 [ChannelType]
 * @property subType    int	子频道子类型 [ChannelSubType]
 * @property position    int	子频道排序，必填；当子频道类型为 子频道分组（[ChannelType]=4）时，必须大于等于 2
 * @property parentId    string	子频道所属分组ID
 * @property privateType    int	子频道私密类型 [PrivateType]
 * @property privateUserIds    string 数组	子频道私密类型成员 ID
 * @property speakPermission    int	子频道发言权限 [SpeakPermission]
 * @property applicationId    string	应用类型子频道应用 [ApplicationId]，仅应用子频道需要该字段
 * */

data class CreateChannel(
    var name: String,
    var type: ChannelType,
    @JsonProperty("sub_type")
    var subType: Int,
    var position: Int,
    @JsonProperty("parent_id")
    var parentId: String,
    @JsonProperty("private_type")
    var privateType: Int,
    @JsonProperty("private_user_ids")
    var privateUserIds: List<String>,
    @JsonProperty("speak_permission")
    var speakPermission: Int,
    @JsonProperty("application_id")
    var applicationId: String?
)

/**
 * @param name    string	子频道名
 * @param position    int	排序
 * @param parentId    string	分组 id
 * @param privateType    int	子频道私密类型 [PrivateType]
 * @param speakPermission    int	子频道发言权限 [SpeakPermission]
 * */
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ModifyChannel(
    var name: String?,
    var position: Int?,
    @JsonProperty("parent_id")
    var parentId: String?,
    @JsonProperty("private_type")
    var privateType: PrivateType?,
    @JsonProperty("speak_permission")
    var speakPermission: Int?
) {
    constructor() : this(null, null, null, null, null)
}

/**
 * @param onlineNums 在线成员数
 * */
data class OnlineNums(
    @JsonProperty("online_nums")
    val onlineNums: Int
)
