package ng.i.sav.qdroid.bot.event

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.time.withTimeoutOrNull
import ng.i.sav.qdroid.infra.client.ApiRequest
import ng.i.sav.qdroid.infra.client.Event
import ng.i.sav.qdroid.infra.client.MessageAuditEventHandler
import ng.i.sav.qdroid.infra.model.MessageAudited
import ng.i.sav.qdroid.infra.model.Payload
import ng.i.sav.qdroid.log.Slf4kt
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.resume

@Component
class MessageAuditResultHandler : MessageAuditEventHandler {
    private val auditResults = ConcurrentHashMap<String, Pair<Boolean, MessageAudited>>()
    private val auditHandler = ConcurrentHashMap<String, CancellableContinuation<Pair<Boolean, MessageAudited>>>()
    override suspend fun onEvent(apiRequest: ApiRequest, event: MessageAudited, payload: Payload<MessageAudited>) {
        log.info("Message: {} audited, result: {}", event.auditId, payload.t)
        val auditedPair = (payload.t == Event.MESSAGE_AUDIT_PASS) to event
        if (auditHandler.contains(event.auditId)) {
            log.debug("Audit handler found, resolve: {}", event.auditId)
            auditHandler.remove(event.auditId)?.resume(auditedPair)
            return
        }
        auditResults[event.auditId] = auditedPair
        delay(Duration.ofMinutes(5).toMillis())
        auditResults.remove(event.auditId)
    }

    suspend fun onAudited(id: String, timeout: Duration = Duration.ofMinutes(1)): Pair<Boolean, MessageAudited>? {
        return auditResults.remove(id) ?: return withTimeoutOrNull(timeout) {
            return@withTimeoutOrNull suspendCancellableCoroutine {
                auditHandler[id] = it
            }
        }.also { auditHandler.remove(id) }
    }

    companion object {
        private val log = Slf4kt.getLogger(MessageAuditResultHandler::class.java)
    }
}
