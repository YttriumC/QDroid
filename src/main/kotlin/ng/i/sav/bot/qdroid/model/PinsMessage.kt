package ng.i.sav.bot.qdroid.model

import com.fasterxml.jackson.annotation.JsonProperty


/**
 * 精华消息对象(PinsMessage)
 *
 * @property guildId    String	频道 id
 * @property channelId    String	子频道 id
 * @property messageIds    String 数组	子频道内精华消息 id 数组
 * */
data class PinsMessage(
    @JsonProperty("guild_id")
    val guildId: String,
    @JsonProperty("channel_id")
    val channelId: String,
    @JsonProperty("message_ids")
    val messageIds: List<String>,
)
