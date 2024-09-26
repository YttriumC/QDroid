import kotlinx.coroutines.runBlocking
import ng.i.sav.qdroid.infra.EnableQDroid
import ng.i.sav.qdroid.infra.client.BotManager
import ng.i.sav.qdroid.log.Slf4kt
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@EnableQDroid
open class Starter {
    fun initSpring(vararg components: Class<*>) = runBlocking {
        log.info("__qdroid__")
        val applicationContext = AnnotationConfigApplicationContext(Starter::class.java, *components)
        val bot = applicationContext.getBean(BotManager::class.java)
        bot.startAsync()
        Runtime.getRuntime().addShutdownHook(Thread {
            bot.shutdown()
        })

    }

    companion object {
        private val log = Slf4kt.getLogger(Starter::class.java)
    }
}
