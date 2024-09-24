package ng.i.sav.qdroid.infra.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

inline fun <reified T> ObjectMapper.toObj(any: Any): T {
    if (any is CharSequence)
        return readValue(any.toString(), object : TypeReference<T>() {})
    return convertValue(any, object : TypeReference<T>() {})
}

fun ObjectMapper.checkLocalDateTimeFormatter() {
    try {
        this.toObj<LocalDateTime>("\"2023-05-22T16:38:04\"")
    } catch (_: Exception) {
        val module = SimpleModule("databind#3110", )
        /*  val deserializer = object : JsonDeserializer<LocalDateTime>() {
              private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ISO_ZONED_DATE_TIME

              override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): LocalDateTime {
                  return LocalDateTime.from(dateTimeFormatter.parse(p.valueAsString))
              }

          }*/

        module.addDeserializer(
            LocalDateTime::class.java,
            object : LocalDateTimeDeserializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME) {})
        module.addSerializer(LocalDateTime::class.java,
            object : LocalDateTimeSerializer(DateTimeFormatter.ISO_LOCAL_DATE_TIME) {})
        registerModule(module)
    }
/*    try {
        this.toObj<LocalDateTime>("\"2023-05-22T16:38:04+08:00\"")
    } catch (e: Exception) {
        throw IllegalStateException("Can not support date time format", e)
    }*/
}
