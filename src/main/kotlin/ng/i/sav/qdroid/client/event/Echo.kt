package ng.i.sav.qdroid.client.event

import ng.i.sav.qdroid.infra.client.AtMessageCreateHandler
import ng.i.sav.qdroid.infra.client.GuildBot
import ng.i.sav.qdroid.infra.model.Message
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
