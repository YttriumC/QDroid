package ng.i.sav.bot.qdroid.model.event


import com.fasterxml.jackson.annotation.JsonProperty

data class MessageDeleteEvent(
    @JsonProperty("message")
    val messageInfo: MessageInfo,
    @JsonProperty("op_user")
    val opUser: OpUser
)

data class OpUser(
    @JsonProperty("id")
    val id: String
)

data class Author(
    @JsonProperty("bot")
    val bot: Boolean,
    @JsonProperty("id")
    val id: String,
    @JsonProperty("username")
    val username: String
)

data class MessageInfo(
    @JsonProperty("author")
    val author: Author,
    @JsonProperty("channel_id")
    val channelId: String,
    @JsonProperty("guild_id")
    val guildId: String,
    @JsonProperty("id")
    val id: String
)
