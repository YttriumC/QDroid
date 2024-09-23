package ng.i.sav.qdroid.infra.model.event


import com.fasterxml.jackson.annotation.JsonProperty

/**
 *
 * */
data class LiveChannelEvent(
    @JsonProperty("guild_id")
    val guildId: String,
    @JsonProperty("channel_id")
    val channelId: String,
    @JsonProperty("channel_type")
    val channelType: Int,
    @JsonProperty("user_id")
    val userId: String
)
