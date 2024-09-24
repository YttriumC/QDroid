package ng.i.sav.qdroid.infra.client

abstract class BotLifecycle {
    open fun onStart(bot: QDroid) {}
    open fun onResume(bot: QDroid) {}
    open fun onAuthenticationFailed(bot: QDroid) {}
    open fun onAuthenticationSuccess(bot: QDroid) {}
    open fun onResumeFailed(bot: QDroid) {}
    open fun onConnectionClosed(bot: QDroid) {}
    open fun onShutdown(bot: QDroid) {}
}
