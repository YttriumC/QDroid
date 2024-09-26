package ng.i.sav.qdroid.bot.event

import ng.i.sav.qdroid.infra.client.Event
import ng.i.sav.qdroid.infra.client.QDroid
import ng.i.sav.qdroid.infra.model.Message
import ng.i.sav.qdroid.infra.model.Payload
import org.springframework.stereotype.Component

@Component
class MessageInterceptorManager {
    private val interceptors: HashMap<String, ArrayList<InterceptExecutor>> = hashMapOf()

    fun onEvent(bot: QDroid, event: Payload<Message>): Boolean {
        var intercepted = false
        interceptors[event.d?.author?.id]?.also { list ->
            val removeList = arrayListOf<InterceptExecutor>()
            list.forEach {
                if (it.acceptEvents().contains(event.t)) {
                    it.onMessage(bot, event.d!!) { removeList.add(it) }
                    intercepted = true
                }
            }
            list.removeAll(removeList.toSet())
        }
        return intercepted
    }

    fun addInterceptUser(userId: String, interceptExecutor: InterceptExecutor) {
        interceptors.compute(userId) { _, v ->
            v?.apply { add(interceptExecutor) } ?: arrayListOf(interceptExecutor)
        }
    }

    interface InterceptExecutor {
        fun acceptEvents(): List<Event>

        fun onMessage(bot: QDroid, message: Message, finish: () -> Unit)
    }
}