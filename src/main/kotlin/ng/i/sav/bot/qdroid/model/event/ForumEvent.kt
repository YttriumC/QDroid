package ng.i.sav.bot.qdroid.model.event


import com.fasterxml.jackson.annotation.JsonProperty
/**
 *
 * */
data class ForumEvent(
    @JsonProperty("guild_id")
    val guildId: String,
    @JsonProperty("channel_id")
    val channelId: String,
    @JsonProperty("author_id")
    val authorId: String
)
