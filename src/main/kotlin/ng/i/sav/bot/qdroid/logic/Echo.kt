package ng.i.sav.bot.qdroid.logic

import ng.i.sav.bot.qdroid.client.AtMessageCreateHandler
import ng.i.sav.bot.qdroid.client.GuildBot
import ng.i.sav.bot.qdroid.model.Message
import org.springframework.stereotype.Component

@Component
class Echo : AtMessageCreateHandler {
    override fun onEvent(bot: GuildBot, event: Message, type: String) {
        println(event.content)
        bot.postChannelsMessages(
            event.channelId,
            event.content?.replace(Regex("<@!\\d+>"), ""),
            msgId = event.id
        )
    }
}
