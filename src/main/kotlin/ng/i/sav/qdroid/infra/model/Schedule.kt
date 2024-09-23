package ng.i.sav.qdroid.infra.model

import ng.i.sav.qdroid.infra.model.RemindType.*
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
 * 日程对象
 *
 * @property id    String	日程 id
 * @property name    String	日程名称
 * @property description    String	日程描述
 * @property startTimestamp    String	日程开始时间戳(ms)
 * @property endTimestamp    String	日程结束时间戳(ms)
 * @property creator    Member	创建者
 * @property jumpChannelId    String	日程开始时跳转到的子频道 id
 * @property remindType    String	日程提醒类型，取值参考RemindType
 *
 * */
data class Schedule(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("name")
    val name: String,
    @JsonProperty("description")
    val description: String,
    @JsonProperty("start_timestamp")
    val startTimestamp: String,
    @JsonProperty("end_timestamp")
    val endTimestamp: String,
    @JsonProperty("creator")
    val creator: Member,
    @JsonProperty("jump_channel_id")
    val jumpChannelId: String,
    @JsonProperty("remind_type")
    val remindType: RemindType,
)

/**
 *  @property NO 0	不提醒
 *  @property ON_START 1	开始时提醒
 *  @property FIVE_MINUTES 2	开始前 5 分钟提醒
 *  @property A_QUARTER 3	开始前 15 分钟提醒
 *  @property HALF_HOUR 4	开始前 30 分钟提醒
 *  @property AN_HOUR 5	开始前 60 分钟提醒
 *
 * */
@JsonDeserialize(using = Deserializer::class)
@JsonSerialize(using = Serializer::class)
enum class RemindType(
    val id: Int,
    val desc: String
) {
    NO(0, "不提醒"),
    ON_START(1, "开始时提醒"),
    FIVE_MINUTES(2, "开始前 5 分钟提醒"),
    A_QUARTER(3, "开始前 15 分钟提醒"),
    HALF_HOUR(4, "开始前 30 分钟提醒"),
    AN_HOUR(5, "开始前 60 分钟提醒"),
    UNKNOWN(-1, "未知时间");

    class Deserializer : JsonDeserializer<RemindType>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): RemindType {
            val i = p.valueAsInt
            return RemindType.entries.find { it.id == i } ?: UNKNOWN
        }

    }

    class Serializer : JsonSerializer<RemindType>() {
        override fun serialize(value: RemindType, gen: JsonGenerator, serializers: SerializerProvider?) {
            gen.writeNumber(value.id)
        }
    }
}
