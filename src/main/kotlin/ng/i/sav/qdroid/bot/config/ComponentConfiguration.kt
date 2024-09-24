package ng.i.sav.qdroid.bot.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import jakarta.annotation.PostConstruct
import ng.i.sav.qdroid.infra.client.HttpRequestPool
import ng.i.sav.qdroid.infra.client.Intents
import ng.i.sav.qdroid.infra.config.RestClient
import ng.i.sav.qdroid.infra.config.WsClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.web.client.RestTemplate
import org.springframework.web.socket.client.WebSocketClient
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import java.util.concurrent.ThreadFactory

open class ComponentConfiguration(
    @Autowired(required = false)
    private var restTemplate: RestTemplate? = null,
    @Autowired(required = false)
    private var wsClient: WebSocketClient? = null,
    @Autowired(required = false)
    private var objectMapper: ObjectMapper? = null,
    @Autowired(required = false)
    private var threadFactory: ThreadFactory? = null
) {

    @PostConstruct
    open fun init() {

    }

    @Bean
    open fun restClient(): RestClient {
        return when (restTemplate == null) {
            true -> RestClient(RestTemplate())
            false -> RestClient(restTemplate!!)
        }
    }

    @Bean
    open fun wsClient(): WsClient {
        return when (wsClient == null) {
            true -> WsClient(StandardWebSocketClient())
            false -> WsClient(wsClient!!)
        }
    }

    @Bean
    open fun objectMapper(): ObjectMapper {
        return if (objectMapper == null) ObjectMapper().apply {
            this.registerModule(JavaTimeModule())
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        } else objectMapper!!
    }

    @Bean
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
