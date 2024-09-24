package ng.i.sav.qdroid.bot.config

import com.fasterxml.jackson.databind.ObjectMapper
import ng.i.sav.qdroid.infra.client.BotEventDispatcher
import ng.i.sav.qdroid.infra.client.BotLifecycle
import ng.i.sav.qdroid.infra.client.HttpRequestPool
import ng.i.sav.qdroid.infra.client.Intents
import ng.i.sav.qdroid.infra.config.RestClient
import ng.i.sav.qdroid.infra.config.WsClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.util.ResourceUtils
import org.springframework.web.client.RestTemplate
import org.springframework.web.socket.client.WebSocketClient
import java.util.*
import java.util.concurrent.ThreadFactory

@Configuration
@ComponentScan("ng.i.sav.qdroid.bot")
open class BotConfigurer(
    private var restClient: RestClient,
    private var wsClient: WsClient,
    private var httpRequestPool: HttpRequestPool,
    private var intents: Array<Intents>,
    private var objectMapper: ObjectMapper,
    private var lifecycle: List<BotLifecycle>,
    private var eventDispatcher: BotEventDispatcher,
) {
    @Bean
    open fun botConfiguration(): BotConfiguration {
        val token: String
        val appId: String
        val apiHost: String
        val isPrivateBot: Boolean
        val totalShards: Int
        Properties().apply {
            load(ResourceUtils.getFile("${ResourceUtils.CLASSPATH_URL_PREFIX}botApi.properties").reader())
        }.let {
            token = it["token"] as String
            appId = it["appId"] as String
            apiHost = it["apiHost"] as String
            isPrivateBot = (it["isPrivateBot"] as String).toBoolean()
            totalShards = (it["totalShards"] as String).toInt()
        }
        return BotConfiguration(
            appId, token, apiHost, restClient, wsClient,
            httpRequestPool, intents, objectMapper, lifecycle,
            eventDispatcher, totalShards, isPrivateBot = isPrivateBot
        )
    }

}


@Configuration
open class ComponentConfigurer(
    @Autowired(required = false)
    private var restTemplate: RestTemplate? = null,
    @Autowired(required = false)
    private var wsClient: WebSocketClient? = null,
    @Autowired(required = false)
    private var objectMapper: ObjectMapper? = null,
    @Autowired(required = false)
    private var threadFactory: ThreadFactory? = null
) : ComponentConfiguration(restTemplate, wsClient, objectMapper, threadFactory)
