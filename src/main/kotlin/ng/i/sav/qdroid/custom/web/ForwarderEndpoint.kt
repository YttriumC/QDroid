package ng.i.sav.qdroid.custom.web

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.coroutines.runBlocking
import ng.i.sav.qdroid.bot.event.MessageAuditResultHandler
import ng.i.sav.qdroid.bot.event.NamedPersistenceModule
import ng.i.sav.qdroid.custom.inst.Forwarder
import ng.i.sav.qdroid.infra.client.ApiRequest
import ng.i.sav.qdroid.infra.client.ApiRequestFailure
import ng.i.sav.qdroid.infra.util.typeRef
import ng.i.sav.qdroid.log.Slf4kt
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.concurrent.CompletableFuture

@RestController
@RequestMapping("forwarder")
class ForwarderEndpoint(
    namedPersistenceModule: NamedPersistenceModule,
    forwarder: Forwarder,
    private val apiRequest: ApiRequest,
    private val messageAuditResultHandler: MessageAuditResultHandler
) {
    private val persistence = namedPersistenceModule.getPersistence(forwarder)

    @PostMapping("msg")
    suspend fun msg(@RequestBody webhookRequest: WebhookRequest): String {
        val forwarderConfig = runBlocking { persistence.get("forwarderInfo", typeRef<List<Forwarder.ForwarderConfig>>()) }
        val config = forwarderConfig?.firstOrNull { it.token == webhookRequest.token }
        if (config == null) {
            return "failed"
        }
        return runCatching {
            apiRequest.postChannelsMessages(config.channelId, webhookRequest.text).id
        }.exceptionOrNull()?.let {
            if (it is ApiRequestFailure) {
                if (it.errorData?.code == 304023) {
                    val data = it.errorData.data
                    data as HashMap<*, *>
                    val auditId = (data["message_audit"] as HashMap<*, *>)["audit_id"] as String
                    log.warn("Message auditing, auditId: {}", auditId)
                    if (messageAuditResultHandler.onAudited(auditId)?.first == true) {
                        return "success"
                    }
                }
            }
            "failed"
        } ?: "success"

    }

    data class WebhookRequest(
        @JsonProperty("token")
        val token: String,
        @JsonProperty("text")
        val text: String
    )

    companion object {
        private var log = Slf4kt.getLogger(ForwarderEndpoint::class.java)
    }
}
