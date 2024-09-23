package ng.i.sav.bot.qdroid.model.event


import com.fasterxml.jackson.annotation.JsonProperty
/**
 * 在 [ng.i.sav.bot.qdroid.model.Channel] 的部分字段基础上，增加 op_user_id 代表操作人。
 * 发送时机
 * - 子频道被创建
 * - 子频道信息变更
 * - 子频道被删除
 * */
data class ChannelEvent(
    @JsonProperty("guild_id")
    val guildId: String,
    @JsonProperty("id")
    val id: String,
    @JsonProperty("name")
    val name: String,
    @JsonProperty("op_user_id")
    val opUserId: String,
    @JsonProperty("owner_id")
    val ownerId: String,
    @JsonProperty("sub_type")
    val subType: Int,
    @JsonProperty("type")
    val type: Int
)
