package ng.i.sav.qdroid.bot.event

import ng.i.sav.qdroid.infra.client.ApiRequest
import ng.i.sav.qdroid.infra.client.AtMessageCreateHandler
import ng.i.sav.qdroid.infra.model.Message
import ng.i.sav.qdroid.infra.model.Payload

class Echo : AtMessageCreateHandler {
    override fun onEvent(apiRequest: ApiRequest, event: Message, payload: Payload<Message>) {
        println(event.content)
        apiRequest.postChannelsMessages(
            event.channelId,
            event.content?.replace(Regex("<@!\\d+>"), ""),
            msgId = event.id
        )
    }
}
