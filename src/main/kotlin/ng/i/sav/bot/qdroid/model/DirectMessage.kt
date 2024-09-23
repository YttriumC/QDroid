package ng.i.sav.bot.qdroid.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * 私信会话对象（DMS）
 * @property guildId    string	私信会话关联的频道 id
 * @property channelId    string	私信会话关联的子频道 id
 * @property createTime    string	创建私信会话时间戳
 * */
data class DirectMessage(
    @JsonProperty("guild_id")
    val guildId: String,
    @JsonProperty("channel_id")
    val channelId: String,
    @JsonProperty("create_time")
    val createTime: String,
)
