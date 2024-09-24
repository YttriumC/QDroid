import ng.i.sav.qdroid.bot.config.BotConfigurer
import ng.i.sav.qdroid.bot.event.Status
import ng.i.sav.qdroid.infra.Starter
import ng.i.sav.qdroid.infra.client.AtMessageCreateHandler
import ng.i.sav.qdroid.infra.client.QDroid
import ng.i.sav.qdroid.infra.model.Message
import ng.i.sav.qdroid.infra.model.Payload
import ng.i.sav.qdroid.log.Slf4kt
import org.springframework.context.annotation.Import

@Import(Status::class)
open class Configurer

val log = Slf4kt.getLogger("BotManagerTestKt")
fun main() {

    Starter().initSpring(BotConfigurer::class.java, Configurer::class.java, object : AtMessageCreateHandler {
        override fun onEvent(bot: QDroid, event: Message, payload: Payload<Message>) {
            log.info("{}", event)
        }

    }::class.java)
}
