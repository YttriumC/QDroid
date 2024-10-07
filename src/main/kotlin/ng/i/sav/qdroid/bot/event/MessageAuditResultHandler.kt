package ng.i.sav.qdroid.bot.event

import ng.i.sav.qdroid.infra.client.ApiRequest
import ng.i.sav.qdroid.infra.client.Event
import ng.i.sav.qdroid.infra.client.MessageAuditEventHandler
import ng.i.sav.qdroid.infra.model.MessageAudited
import ng.i.sav.qdroid.infra.model.Payload
import ng.i.sav.qdroid.log.Slf4kt
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

@Component
class MessageAuditResultHandler : MessageAuditEventHandler {
    private val auditResults = ConcurrentHashMap<String, Pair<Boolean, MessageAudited>>()
    private val auditHandler = ConcurrentHashMap<String, (Boolean, MessageAudited) -> Unit>()
    override fun onEvent(apiRequest: ApiRequest, event: MessageAudited, payload: Payload<MessageAudited>) {
        log.info("Message: {} audited, result: {}", event.auditId, payload.t)
        auditResults[event.auditId] = (payload.t == Event.MESSAGE_AUDIT_PASS) to event
        val removeSet = mutableSetOf<String>()
        // amount of data may cause performance issue
        // caution race condition
        auditResults.forEach { (t, u) ->
            auditHandler[t]?.let {
                removeSet.add(t)
                log.debug("Resolve audit: {}", t)
                it(u.first, u.second)
                auditHandler.remove(t)
            }
            if (LocalDateTime.now().minusMinutes(5) > u.second.auditTime) {
                log.debug("Audit result outdated: {}", u.first)
                removeSet.add(t)
            }
        }
    }

    fun onAudited(id: String, callback: (Boolean, MessageAudited) -> Unit) {
        // caution race condition
        log.debug("Add audit id: {}", id)
        auditResults[id]?.let {
            auditResults.remove(id)
            callback(it.first, it.second)
        } ?: let { auditHandler[id] = callback }
    }

    companion object {
        private val log = Slf4kt.getLogger(MessageAuditResultHandler::class.java)
    }
}
