package ng.i.sav.qdroid.bot.event

import ng.i.sav.qdroid.bot.event.MessageInterceptorManager.InterceptExecutor
import ng.i.sav.qdroid.infra.client.ApiRequest
import ng.i.sav.qdroid.infra.model.Message

interface MessageInstruction : Named {
    fun getInstructions(): List<String>
    fun execute(
        apiRequest: ApiRequest,
        addInterceptor: (userId: String, interceptExecutor: InterceptExecutor) -> Unit,
        remainContent: String?,
        message: Message,
        eventId: String?
    )
}
