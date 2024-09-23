package ng.i.sav.bot.qdroid.model.event


import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 发送时机
 * - 机器人被加入到某个频道的时候
 * - 频道信息变更, 事件内容为变更后的数据
 * - 频道被解散, 事件内容为变更前的数据
 * - 机器人被移除, 事件内容为变更前的数据
 * */
data class GuildEvent(
    @JsonProperty("description")
    val description: String,
    @JsonProperty("icon")
    val icon: String,
    @JsonProperty("id")
    val id: String,
    @JsonProperty("joined_at")
    val joinedAt: String,
    @JsonProperty("max_members")
    val maxMembers: Int,
    @JsonProperty("member_count")
    val memberCount: Int,
    @JsonProperty("name")
    val name: String,
    @JsonProperty("op_user_id")
    val opUserId: String,
    @JsonProperty("owner_id")
    val ownerId: String
)
