package ng.i.sav.qdroid.infra

import kotlinx.coroutines.runBlocking
import ng.i.sav.qdroid.infra.client.BotManager
import ng.i.sav.qdroid.log.Slf4kt
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@ComponentScan
@Configuration
open class Starter {
    fun initSpring(vararg configurations: Class<*>) = runBlocking {
        val applicationContext = AnnotationConfigApplicationContext(Starter::class.java, *configurations)
        val bot = applicationContext.getBean(BotManager::class.java)
        bot.startAsync()
        log.info("__qdroid__")
        Runtime.getRuntime().addShutdownHook(Thread {
            bot.shutdown()
        })

    }

    companion object {
        val log = Slf4kt.getLogger(Starter::class.java)
    }
}
