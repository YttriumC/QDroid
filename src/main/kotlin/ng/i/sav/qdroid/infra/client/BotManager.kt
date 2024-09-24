package ng.i.sav.qdroid.infra.client

import com.fasterxml.jackson.databind.ObjectMapper
import ng.i.sav.qdroid.bot.config.BotConfiguration
import ng.i.sav.qdroid.infra.config.RestClient
import ng.i.sav.qdroid.infra.config.WsClient
import ng.i.sav.qdroid.infra.util.checkLocalDateTimeFormatter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URL

/**
 * bot工厂
 * */
@Component
class BotManager(
    configuration: BotConfiguration
) {
    private val appId: String = configuration.appId

    private val token: String = configuration.token

    private val apiHost: String = configuration.apiHost
    private val restClient: RestClient = configuration.restClient
    private val wsClient: WsClient = configuration.wsClient
    private val httpRequestPool: HttpRequestPool = configuration.httpRequestPool
    private val intents: Array<Intents> = configuration.intents
    private val objectMapper: ObjectMapper = configuration.objectMapper
    private val lifecycle: List<BotLifecycle> = configuration.lifecycle
    private val eventDispatcher: BotEventDispatcher = configuration.eventDispatcher

    private val totalShards: Int = configuration.totalShards
    private val shardsRange: IntRange = configuration.shardsRange

    private val isPrivateBot: Boolean = configuration.isPrivateBot

    /*
     * TODO: secondary constructor for [ng.i.sav.bot.qdroid.config.BotConfig]
     * */

    val shards get() = shardsRange.last - shardsRange.first

    private val botArray = ArrayList<QDroid>(shards)

    init {
        if (shardsRange.isEmpty() || shardsRange.last >= totalShards) {
            throw IllegalArgumentException("wrong shards range")
        }
    }

    fun startAsync(): ArrayList<QDroid> {
        log.info("start bot...")
        log.info(
            "This instance has {} shards, from {} to {}",
            shards,
            shardsRange.first,
            shardsRange.last
        )
        var intents = this.intents.toInt()
        if (!isPrivateBot) intents = intents - Intents.GUILD_MESSAGES - Intents.FORUMS_EVENT
        for (i in shardsRange) {
            log.debug("Create bot with appID: {}, appToken: {}, apiURL: {}, shard: {}", appId, token, apiHost, i)
            QDroid(
                appId, token, URL(apiHost), restClient,
                wsClient, httpRequestPool, intents, totalShards,
                objectMapper, lifecycle, eventDispatcher, i
            ).let(
                botArray::add
            )
        }

        botArray.forEach(QDroid::start)

        return botArray
    }

    fun shutdown() {
        botArray.forEach {
            it.shutdown()
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(BotManager::class.java)
    }
}
