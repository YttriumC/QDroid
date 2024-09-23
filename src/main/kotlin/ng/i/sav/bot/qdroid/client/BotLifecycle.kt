package ng.i.sav.bot.qdroid.client

abstract class BotLifecycle {
    open fun onStart(bot: GuildBot) {}
    open fun onResume(bot: GuildBot) {}
    open fun onAuthenticationFailed(bot: GuildBot) {}
    open fun onAuthenticationSuccess(bot: GuildBot) {}
    open fun onResumeFailed(bot: GuildBot) {}
    open fun onConnectionClosed(bot: GuildBot) {}
    open fun onShutdown(bot: GuildBot) {}
}
