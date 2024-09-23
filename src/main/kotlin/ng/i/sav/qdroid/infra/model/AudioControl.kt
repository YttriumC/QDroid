package ng.i.sav.qdroid.infra.model

import ng.i.sav.qdroid.infra.model.Status.*
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize

/**
 * @property audioUrl    string	音频数据的url status为0时传
 * @property text    string	状态文本（比如：简单爱-周杰伦），可选，status为0时传，其他操作不传
 * @property status    [Status]	播放状态，参考 [Status]
 * */
data class AudioControl(
    @JsonProperty("audio_url")
    val audioUrl: String,
    @JsonProperty("text")
    val text: String?,
    @JsonProperty("status")
    val status: Status
)


/**
 * @property START 0 开始播放操作
 * @property PAUSE 1 暂停播放操作
 * @property RESUME 2 继续播放操作
 * @property STOP 3 停止播放操作
 */
@JsonDeserialize(using = Status.Deserializer::class)
@JsonSerialize(using = Status.Serializer::class)
enum class Status(
    val value: Int,
    val description: String,
) {
    START(0, "开始播放操作"),
    PAUSE(1, "暂停播放操作"),
    RESUME(2, "继续播放操作"),
    STOP(3, "停止播放操作"),
    UNKNOWN(-1, "未知状态");

    class Deserializer : JsonDeserializer<Status>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Status {
            val i = p.valueAsInt
            return Status.entries.find { it.value == i } ?: UNKNOWN
        }

    }

    class Serializer : JsonSerializer<Status>() {
        override fun serialize(value: Status, gen: JsonGenerator, serializers: SerializerProvider?) {
            gen.writeNumber(value.value)
        }
    }
}

/**
 * @property guildId string 频道id
 * @property channelId string 子频道id
 * @property audioUrl string 音频数据的url status为0时传
 * @property text string 状态文本（比如：简单爱-周杰伦），可选，status为0时传，其他操作不传
 */
data class AudioAction(
    @JsonProperty("guild_id")
    val guildId: String,
    @JsonProperty("channel_id")
    val channelId: String,
    @JsonProperty("audio_url")
    val audioUrl: String,
    @JsonProperty("text")
    val text: String? = null
)
