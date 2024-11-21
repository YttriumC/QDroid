package ng.i.sav.qdroid.bot.event

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
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
    private val lock = Mutex() // ensure consume or produce not doing on the same time
    private val auditLogMap = ConcurrentHashMap<String, AuditLog>()
    override suspend fun onEvent(apiRequest: ApiRequest, event: MessageAudited, payload: Payload<MessageAudited>) {
        log.info("Message: {} audited, result: {}, content: {}", event.auditId, payload.t, event)
        println(auditHandler)
        val auditedPair = (payload.t == Event.MESSAGE_AUDIT_PASS) to event
        lock.lock()
        if (auditHandler.containsKey(event.auditId)) {
            log.debug("Audit handler found, resolve: {}", event.auditId)
            auditHandler.remove(event.auditId)
                ?.resume(auditedPair)
            lock.unlock()
            return
        }
        /*withTimeoutOrNull(Duration.ofMinutes(1).toMillis()) {
            suspendCoroutine {
                if (auditResults.contains(event.auditId)) {
                    it.resume(auditedPair)
                } else {
                    auditResults[event.auditId] = auditedPair
                }
            }

        }*/
        auditResults[event.auditId] = auditedPair
        lock.unlock()
        delay(Duration.ofMinutes(5).toMillis())
        auditResults.remove(event.auditId)
    }

    suspend fun onAudited(id: String, timeout: Duration = Duration.ofMinutes(1)): Pair<Boolean, MessageAudited>? {
        lock.lock()
        return auditResults.remove(id)?.also { lock.unlock() } ?: return withTimeoutOrNull(timeout) {
            log.debug("onAudited without data")
            return@withTimeoutOrNull suspendCancellableCoroutine {
                lock.unlock()
                /*auditResults.remove(id)?.let { r -> it.resume(r) } ?: auditHandler.put(id, it)*/
                auditHandler[id] = it
            }
        }.also { auditHandler.remove(id) }
    }


    data class AuditLog(
        var continuation: CancellableContinuation<Pair<Boolean, MessageAudited>>? = null,
        var value: Pair<Boolean, MessageAudited>? = null,
    )

    companion object {
        private val log = Slf4kt.getLogger(MessageAuditResultHandler::class.java)
    }
}
