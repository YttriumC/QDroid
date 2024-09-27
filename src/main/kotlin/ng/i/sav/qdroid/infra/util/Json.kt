package ng.i.sav.qdroid.infra.util

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import ng.i.sav.qdroid.log.Slf4kt
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val log = Slf4kt.getLogger("ng.i.sav.qdroid.infra.util.JsonKt")
inline fun <reified T> ObjectMapper.toObj(any: Any): T {
    if (any is CharSequence)
        return readValue(any.toString(), object : TypeReference<T>() {})
    return convertValue(any, object : TypeReference<T>() {})
}

fun ObjectMapper.checkLocalDateTimeFormatter() {
    try {
        this.toObj<LocalDateTime>("\"2023-05-22T16:38:04+08:00\"")
    } catch (_: Exception) {
        log.info("Check local date time filed, auto register formatter")
        val module = SimpleModule("databind#3110")
        module.addDeserializer(
            LocalDateTime::class.java,
            LocalDateTimeDeserializer(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        )
        module.addSerializer(
            LocalDateTime::class.java,
            LocalDateTimeSerializer(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
        )
        registerModule(module)
        this.toObj<LocalDateTime>("\"2024-09-24T16:39:42+08:00\"")
    }
}

inline fun <reified T> typeRef() = object : TypeReference<T>() {}
