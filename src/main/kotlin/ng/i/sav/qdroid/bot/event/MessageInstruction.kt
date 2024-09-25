package ng.i.sav.qdroid.bot.event

import ng.i.sav.qdroid.infra.client.QDroid
import ng.i.sav.qdroid.infra.model.Message

interface MessageInstruction {
    fun getInstructions(): List<String>
    fun execute(bot: QDroid, interceptor: MessageInterceptor, remainContent: String?, event: Message, eventId: String?)
}