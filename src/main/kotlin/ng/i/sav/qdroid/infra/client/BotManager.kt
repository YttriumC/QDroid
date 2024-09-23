package ng.i.sav.qdroid.infra.client

import ng.i.sav.qdroid.infra.config.RestClient
import ng.i.sav.qdroid.infra.config.WsClient
import ng.i.sav.qdroid.infra.util.checkLocalDateTimeFormatter
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URL

/**
 * bot工厂
 * */
@Component
class BotManager(
    @Value("")
    private val appId: String,
    @Value("")
    private val token: String,
    @Value("")
    private val apiHost: String,
    private val restClient: RestClient,
    private val wsClient: WsClient,
    private val httpRequestPool: HttpRequestPool,
    private val intents: Array<Intents>,
    private val objectMapper: ObjectMapper,
    private val lifecycle: List<BotLifecycle>,
    private val eventDispatcher: BotEventDispatcher,
    @Value("")
    private val totalShards: Int = 1,
    private val shardsRange: IntRange = IntRange(0, totalShards - 1),
    @Value("")
    private val isPrivateBot: Boolean = false,
) {
    /*
     * TODO: secondary constructor for [ng.i.sav.bot.qdroid.config.BotConfig]
     * */

    val shards get() = shardsRange.last - shardsRange.first

    private val botArray = ArrayList<GuildBot>(shards)

    init {
        if (shardsRange.isEmpty() || shardsRange.last >= totalShards) {
            throw IllegalArgumentException("wrong shards range")
        }
        objectMapper.checkLocalDateTimeFormatter()
    }

    fun startAsync(): ArrayList<GuildBot> {
        log.info("start bot...")
        log.info(
            "current shards: {}, from {} to {}",
            shards,
            shardsRange.first,
            shardsRange.last
        )
        var intents = this.intents.toInt()
        if (!isPrivateBot) intents = intents - Intents.GUILD_MESSAGES - Intents.FORUMS_EVENT
        for (i in shardsRange) {
            log.debug("Create bot with appID: {}, appToken: {}, apiURL: {}, shard: {}", appId, token, apiHost, i)
            GuildBot(
                appId,
                token,
                URL(apiHost),
                restClient,
                wsClient,
                httpRequestPool,
                intents,
                totalShards,
                objectMapper,
                lifecycle,
                eventDispatcher,
                i
            ).let(
                botArray::add
            )
        }

        botArray.forEach(GuildBot::start)
        /* GlobalScope.launch {
             delay(60_000)
             repeat(Int.MAX_VALUE) {
                 delay(5_000)
                 log.debug("Health check")
                 botArray.forEach {
                     if (it.state != GuildBot.State.CONNECTED) {
                         it.start()
                     }
                 }
             }
         }*/
        return botArray
    }

    fun shutdown() {
        botArray.forEach {

        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(BotManager::class.java)
    }
}
