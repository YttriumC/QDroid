package ng.i.sav.qdroid.bot.config

import ng.i.sav.qdroid.infra.client.Intents

open class BotConfiguration(
    var appId: String,
    var token: String,
    var apiHost: String,
    var intents: Array<Intents>,
    var totalShards: Int = 1,
    var shardsRange: IntRange = 0..<totalShards,
    var isPrivateBot: Boolean = false,
)
