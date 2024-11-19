package ng.i.sav.qdroid.infra.config

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import jakarta.annotation.PostConstruct
import ng.i.sav.qdroid.infra.client.ApiRequestFailure
import ng.i.sav.qdroid.infra.client.HttpRequestPool
import ng.i.sav.qdroid.infra.client.Intents
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.ClientHttpResponse
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.*
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.ThreadFactory

@Configuration
open class ComponentConfiguration(
    @Autowired(required = false)
    private var threadFactory: ThreadFactory?
) {

    @PostConstruct
    open fun init() {

    }

    private val objectMapper = ObjectMapper().apply {
        this.registerModule(JavaTimeModule())
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        configure(JsonParser.Feature.INCLUDE_SOURCE_IN_LOCATION, true)
        val module = SimpleModule("databind#310")

        module.addDeserializer(
            LocalDateTime::class.java,
            LocalDateTimeDeserializer(
                DateTimeFormatter.ISO_OFFSET_DATE_TIME
            )
        )

        module.addSerializer(
            LocalDateTime::class.java,
            LocalDateTimeSerializer(
                DateTimeFormatter.ISO_OFFSET_DATE_TIME
            )
        )
        registerModule(module)
    }

    open fun getObjectMapper() = objectMapper

    @Bean
    open fun httpRequestPool(): HttpRequestPool {
        return HttpRequestPool(1, threadFactory)
    }
}
