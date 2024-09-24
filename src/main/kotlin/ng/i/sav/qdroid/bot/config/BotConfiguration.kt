package ng.i.sav.qdroid.bot.config

import com.fasterxml.jackson.databind.ObjectMapper
import ng.i.sav.qdroid.infra.client.BotEventDispatcher
import ng.i.sav.qdroid.infra.client.BotLifecycle
import ng.i.sav.qdroid.infra.client.HttpRequestPool
import ng.i.sav.qdroid.infra.client.Intents
import ng.i.sav.qdroid.infra.config.RestClient
import ng.i.sav.qdroid.infra.config.WsClient

open class BotConfiguration(
    var appId: String,
    var token: String,
    var apiHost: String,
    var restClient: RestClient,
    var wsClient: WsClient,
    var httpRequestPool: HttpRequestPool,
    var intents: Array<Intents>,
    var objectMapper: ObjectMapper,
    var lifecycle: List<BotLifecycle>,
    var eventDispatcher: BotEventDispatcher,
    var totalShards: Int = 1,
    var shardsRange: IntRange = IntRange(0, totalShards - 1),
    var isPrivateBot: Boolean = false,
)
