package ng.i.sav.qdroid.infra.config

import ng.i.sav.qdroid.infra.client.Intents
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import org.springframework.web.socket.client.WebSocketClient
import org.springframework.web.socket.client.standard.StandardWebSocketClient

@Configuration
open class SpringConfig {
    @Autowired(required = false)
    private var restTemplate: RestTemplate? = null

    @Autowired(required = false)
    private var wsClient: WebSocketClient? = null

    @Autowired
    private lateinit var botConfig: BotConfig

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
        return ObjectMapper()
    }

    @Bean
    open fun shardsRange(): IntRange {
        return IntRange(0, 0)
    }

    @Bean
    open fun intents(): Array<Intents> {
        return arrayOf()
    }
}
