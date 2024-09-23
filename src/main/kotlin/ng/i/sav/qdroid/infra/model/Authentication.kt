package ng.i.sav.qdroid.infra.model


import com.fasterxml.jackson.annotation.JsonProperty

data class Authentication(
    @JsonProperty("token")
    val token: String,
    @JsonProperty("intents")
    val intents: Int,
    @JsonProperty("shard")
    val shard: Array<Int>,
    @JsonProperty("properties")
    val properties: Map<String, String>? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Authentication

        if (token != other.token) return false
        if (intents != other.intents) return false
        if (!shard.contentEquals(other.shard)) return false
        if (properties != other.properties) return false

        return true
    }

    override fun hashCode(): Int {
        var result = token.hashCode()
        result = 31 * result + intents
        result = 31 * result + shard.contentHashCode()
        result = 31 * result + properties.hashCode()
        return result
    }
}
