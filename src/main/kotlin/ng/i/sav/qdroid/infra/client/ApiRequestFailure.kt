package ng.i.sav.qdroid.infra.client

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.springframework.http.HttpStatusCode

class ApiRequestFailure(
    override val message: String? = null,
    val httpStatus: HttpStatusCode? = null,
    val errorData: ErrorData? = null,
) : RuntimeException(message) {

    @JsonDeserialize(using = ErrorDataDeserializer::class)
    data class ErrorData(
        @JsonProperty("code")
        val code: Int,
        @JsonProperty("message")
        val message: String,
        @JsonProperty("data")
        val data: Any? = null,
        @JsonProperty("err_code")
        val errCode: Int? = null,
        @JsonProperty("trace_id")
        val traceId: String? = null
    )

    class ErrorDataDeserializer : JsonDeserializer<ErrorData>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ErrorData {
            val rootNode = ctxt.readTree(p)
            val code = rootNode["code"]?.asInt()
            val message = rootNode["message"]?.asText()
            val data = rootNode["data"]
            val errCode = rootNode["err_code"]?.asInt()
            val traceId = rootNode["trace_id"]?.asText()
            return ErrorData(
                code ?: -1,
                message ?: "UNKNOWN",
                ctxt.readTreeAsValue(data, ErrorCodeMap[code]?.second ?: Any::class.java),
                errCode,
                traceId
            )
        }
    }

}

