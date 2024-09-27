package ng.i.sav.qdroid.infra.client

import com.fasterxml.jackson.databind.ObjectMapper
import ng.i.sav.qdroid.bot.config.BotConfiguration
import ng.i.sav.qdroid.infra.config.ComponentConfiguration
import ng.i.sav.qdroid.infra.config.WsClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URL
import java.time.LocalDateTime

/**
 * bot工厂
 * */
@Component
class BotManager(
    configuration: BotConfiguration,
    componentConfiguration: ComponentConfiguration,
    private val lifecycle: List<BotLifecycle>,
    private val statelessApiRequest: StatelessApiRequest,
    private val eventDispatcher: BotEventDispatcher,
) {
    private val appId: String = configuration.appId

    private val token: String = configuration.token

    private val apiHost: String = configuration.apiHost
    private val wsClient: WsClient = componentConfiguration.wsClient
    private val intents: Array<Intents> = configuration.intents
    private val objectMapper: ObjectMapper = componentConfiguration.getObjectMapper()
    private val totalShards: Int = configuration.totalShards
    private val shardsRange: IntRange = configuration.shardsRange

    private val isPrivateBot: Boolean = configuration.isPrivateBot

    val startTime: LocalDateTime get() = if (this::_startTime.isInitialized) _startTime else LocalDateTime.MIN
    private lateinit var _startTime: LocalDateTime

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
        _startTime = LocalDateTime.now()
        var intents = this.intents.toInt()
        if (!isPrivateBot) intents = intents - Intents.GUILD_MESSAGES - Intents.FORUMS_EVENT
        for (i in shardsRange) {
            log.debug("Create bot with appID: {}, appToken: {}, apiURL: {}, shard: {}", appId, token, apiHost, i)
            QDroid(
                appId, token, URL(apiHost),
                wsClient, intents, totalShards,
                objectMapper, lifecycle, eventDispatcher, i, statelessApiRequest
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
