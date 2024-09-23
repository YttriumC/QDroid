package ng.i.sav.qdroid.infra.model

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
 * @property userId    string    用户ID
 * @property guildId    string    频道ID
 * @property channelId    string    子频道ID
 * @property target    [ReactionTarget]    表态对象
 * @property emoji    [Emoji]    表态所用表情
 * */
data class MessageReaction(
    @JsonProperty("user_id")
    val userId: String,
    @JsonProperty("guild_id")
    val guildId: String,
    @JsonProperty("channel_id")
    val channelId: String,
    @JsonProperty("target")
    val target: ReactionTarget,
    @JsonProperty("emoji")
    val emoji: Emoji
)

/**
 * @property id    String    表态对象ID
 * @property type    [ReactionTargetType]    表态对象类型，参考 ReactionTargetType
 * */
data class ReactionTarget(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("type")
    val type: ReactionTargetType
)

/**
 * 0	消息
 * 1	帖子
 * 2	评论
 * 3	回复
 * */
@JsonDeserialize(using = ReactionTargetType.Deserializer::class)
@JsonSerialize(using = ReactionTargetType.Serializer::class)
enum class ReactionTargetType(
    val type: Int,
    val desc: String
) {
    MESSAGE(0, "消息"),
    POST(1, "帖子"),
    COMMENT(2, "评论"),
    REPLY(3, "回复"),
    UNKNOWN(-1, "未知类型");

    class Deserializer : JsonDeserializer<ReactionTargetType>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ReactionTargetType {
            val i = p.valueAsInt
            return ReactionTargetType.entries.find { it.type == i } ?: UNKNOWN
        }

    }

    class Serializer : JsonSerializer<ReactionTargetType>() {
        override fun serialize(value: ReactionTargetType, gen: JsonGenerator, serializers: SerializerProvider?) {
            gen.writeNumber(value.type)
        }
    }
}

/**
 * @property users	array	用户对象，参考 User，会返回 id, username, avatar
 * @property cookie	string	分页参数，用于拉取下一页
 * @property isEnd	bool	是否已拉取完成到最后一页，true代表完成
 * */
data class ReactionList(
    @JsonProperty("cookie")
    val cookie: String?,
    @JsonProperty("is_end")
    val isEnd: Boolean,
    @JsonProperty("users")
    val users: List<ReactionUser>
)

data class ReactionUser(
    @JsonProperty("avatar")
    val avatar: String,
    @JsonProperty("id")
    val id: String,
    @JsonProperty("username")
    val username: String
)
