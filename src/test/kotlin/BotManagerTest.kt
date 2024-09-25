import ng.i.sav.qdroid.bot.config.BotConfigurer
import ng.i.sav.qdroid.bot.event.Status
import ng.i.sav.qdroid.cutom.CustomConfig
import ng.i.sav.qdroid.infra.Starter
import ng.i.sav.qdroid.infra.client.AtMessageCreateHandler
import ng.i.sav.qdroid.infra.client.QDroid
import ng.i.sav.qdroid.infra.model.Message
import ng.i.sav.qdroid.infra.model.Payload
import ng.i.sav.qdroid.log.Slf4kt
import org.slf4j.event.Level
import org.springframework.context.annotation.Import

@Import(Status::class)
open class Configurer

private val log = Slf4kt.getLogger("BotManagerTestKt")
fun main() {

    Slf4kt.setLevel(Level.DEBUG)
    Starter().initSpring(BotConfigurer::class.java, Configurer::class.java, CustomConfig::class.java)
}
