package ng.i.sav.qdroid.bot.lifecycle

import ng.i.sav.qdroid.infra.client.BotLifecycle
import ng.i.sav.qdroid.infra.client.QDroid
import ng.i.sav.qdroid.log.Slf4kt
import org.slf4j.event.Level
import org.springframework.stereotype.Component

@Component
class DefaultLifecycle : BotLifecycle() {
    override fun onAuthenticationSuccess(bot: QDroid) {
       log.info("Bot started: {}",bot)
    }

    companion object {
        private val log = Slf4kt.getLogger(DefaultLifecycle::class.java)
    }
}

