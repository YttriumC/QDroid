package ng.i.sav.qdroid.bot.config

import ng.i.sav.qdroid.infra.client.BotEventDispatcher
import ng.i.sav.qdroid.infra.client.BotLifecycle
import ng.i.sav.qdroid.infra.client.HttpRequestPool
import ng.i.sav.qdroid.infra.client.Intents
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.util.ResourceUtils
import java.util.*

@Configuration
@ComponentScan("ng.i.sav.qdroid.bot")
open class BotConfigurer(
    private var httpRequestPool: HttpRequestPool,
    private var intents: Array<Intents>,
    private var lifecycle: List<BotLifecycle>,
    private var eventDispatcher: BotEventDispatcher,
    private var components: ComponentConfiguration
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
            appId, token, apiHost, components.restClient, components.wsClient,
            httpRequestPool, intents, components.objectMapper, lifecycle,
            eventDispatcher, totalShards = totalShards, isPrivateBot = isPrivateBot
        )
    }

}
