import ng.i.sav.qdroid.bot.config.BotConfigurer
import ng.i.sav.qdroid.bot.event.Status
import ng.i.sav.qdroid.custom.CustomConfig
import ng.i.sav.qdroid.log.Slf4kt
import org.springframework.context.annotation.Import

@Import(Status::class)
open class Configurer

private val log = Slf4kt.getLogger("BotManagerTestKt")
fun main() {

//    Slf4kt.setLevel(Level.DEBUG)
    Starter().initSpring(BotConfigurer::class.java, Configurer::class.java, CustomConfig::class.java)
}
