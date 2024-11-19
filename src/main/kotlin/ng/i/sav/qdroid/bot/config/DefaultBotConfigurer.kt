package ng.i.sav.qdroid.bot.config

import ng.i.sav.qdroid.infra.client.Intents
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.util.ResourceUtils
import java.util.*

@Configuration
open class DefaultBotConfigurer {

    /**
     * When multiple instances are needed, the range should be provided.
     * */
    open fun getShardsRange(): IntRange {
        return 0..<1
    }

    open fun getIntents(): Array<Intents> {
        return Intents.defaultPublicIntents()
    }

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
            appId, token, apiHost, getIntents() + Intents.MESSAGE_AUDIT,
            totalShards = totalShards, shardsRange = getShardsRange(), isPrivateBot = isPrivateBot
        )
    }

}

@ComponentScan("ng.i.sav.qdroid.bot")
@Configuration
open class BotConfigurer : DefaultBotConfigurer()
