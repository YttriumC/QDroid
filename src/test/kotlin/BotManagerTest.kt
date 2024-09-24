import ng.i.sav.qdroid.bot.config.BotConfigurer
import ng.i.sav.qdroid.infra.Starter

fun main() {

    Starter().initSpring(BotConfigurer::class.java)
}
