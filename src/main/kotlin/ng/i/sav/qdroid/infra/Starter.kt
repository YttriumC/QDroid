package ng.i.sav.qdroid.infra

import ng.i.sav.qdroid.infra.client.BotManager
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@ComponentScan
@Configuration
open class Starter {
    fun initSpring() {
        val applicationContext = AnnotationConfigApplicationContext(Starter::class.java)
        applicationContext.refresh()
        val bot = applicationContext.getBean(BotManager::class.java)
        bot.startAsync()
    }
}
