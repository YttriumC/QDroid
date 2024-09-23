package ng.i.sav.bot.qdroid.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 禁言批量成员 响应数据包
 * */
data class MuteResp(
    @JsonProperty("user_ids")
    val userIds: List<String>
)
