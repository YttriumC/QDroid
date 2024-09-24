package ng.i.sav.qdroid.bot.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import jakarta.annotation.PostConstruct
import ng.i.sav.qdroid.bot.event.Status
import ng.i.sav.qdroid.bot.lifecycle.DefaultLifecycle
import ng.i.sav.qdroid.infra.client.BotEventDispatcher
import ng.i.sav.qdroid.infra.client.BotManager
import ng.i.sav.qdroid.infra.client.HttpRequestPool
import ng.i.sav.qdroid.infra.client.Intents
import ng.i.sav.qdroid.infra.config.RestClient
import ng.i.sav.qdroid.infra.config.WsClient
import ng.i.sav.qdroid.log.Slf4kt
import org.slf4j.event.Level
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.socket.client.WebSocketClient
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.*
import java.util.concurrent.ThreadFactory

@Configuration
open class ComponentConfiguration(
    @Autowired(required = false)
    private var threadFactory: ThreadFactory?
) {

    @PostConstruct
    open fun init() {

    }

    val objectMapper = ObjectMapper().apply {
        this.registerModule(JavaTimeModule())
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
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
    val restClient = RestClient(RestTemplate().apply {
        messageConverters.filterIsInstance<MappingJackson2HttpMessageConverter>().firstOrNull()
            ?.let { it.objectMapper = objectMapper } ?: run {
            messageConverters.add(MappingJackson2HttpMessageConverter(objectMapper))
        }
    })


    val wsClient = WsClient(StandardWebSocketClient())


    open fun shardsRange(): IntRange {
        return IntRange(0, 0)
    }

    @Bean
    open fun intents(): Array<Intents> {
        return Intents.allPublicMessages()
    }

    @Bean
    open fun httpRequestPool(): HttpRequestPool {
        return HttpRequestPool(1, threadFactory)
    }
}
