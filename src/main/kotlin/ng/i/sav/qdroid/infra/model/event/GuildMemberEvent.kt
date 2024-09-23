package ng.i.sav.qdroid.infra.model.event


import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime
/**
 * 在 [ng.i.sav.qdroid.infra.model.Member] 基础上，增加 op_user_id 代表操作人。
 *
 * 注：此事件由于开发较早，尚有一些字段未标准化处理，如 joined_at, roles 请开发者适配的时候注意。晚些时候我们也会将这些字段标准化处理。
 * 发送时机
 * - 新用户加入频道
 * - 用户的频道属性发生变化，如频道昵称，或者身份组
 * - 用户离开频道
 * */
data class GuildMemberEvent(
    @JsonProperty("guild_id")
    val guildId: String,
    @JsonProperty("joined_at")
    val joinedAt: LocalDateTime,
    @JsonProperty("nick")
    val nick: String,
    @JsonProperty("op_user_id")
    val opUserId: String,
    @JsonProperty("roles")
    val roles: List<String>,
    @JsonProperty("user")
    val user: Any
)
