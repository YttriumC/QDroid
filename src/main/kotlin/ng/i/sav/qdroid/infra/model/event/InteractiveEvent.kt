package ng.i.sav.qdroid.infra.model.event


import com.fasterxml.jackson.annotation.JsonProperty

data class InteractiveEvent(
    @JsonProperty("chat_type")
    val chatType: Int,
    @JsonProperty("data")
    val `data`: Data,
    @JsonProperty("id")
    val id: String,
    @JsonProperty("type")
    val type: Int,
    @JsonProperty("version")
    val version: Int
) {
    data class Data(
        @JsonProperty("resolved")
        val resolved: Resolved,
        @JsonProperty("type")
        val type: Int
    ) {
        data class Resolved(
            @JsonProperty("button_data")
            val buttonData: String,
            @JsonProperty("button_id")
            val buttonId: String,
            @JsonProperty("user_id")
            val userId: String
        )
    }
}
