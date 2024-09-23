package ng.i.sav.bot.qdroid.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

/**
 * @property guildId    string	频道id
 * @property user    User	用户的频道基础信息
 * @property nick    string	用户的昵称
 * @property roles    string 数组	用户在频道内的身份
 * @property joinedAt    ISO8601 timestamp	用户加入频道的时间
 * */
data class Member(
    @JsonProperty("guild_id")
    val guildId: String?,
    @JsonProperty("user")
    val user: User?,
    @JsonProperty("nick")
    val nick: String,
    @JsonProperty("roles")
    val roles: List<String>,
    @JsonProperty("joined_at")
    val joinedAt: LocalDateTime
) {
}

/**
 * @property data [Member] 对象数组。	一组用户信息对象
 * @property next string    下一次请求的分页标识
 *
 * */
data class RolesResp(
    @JsonProperty("data")
    val data: List<Member>,
    @JsonProperty("next")
    val next: String
)
