package ng.i.sav.qdroid.custom.web

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.coroutines.runBlocking
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

@RestController
@RequestMapping("forwarder")
class ForwarderEndpoint(
    namedPersistenceModule: NamedPersistenceModule,
    forwarder: Forwarder,
    private val apiRequest: ApiRequest
) {
    private val persistence = namedPersistenceModule.getPersistence(forwarder)

    @PostMapping("msg")
    fun msg(@RequestBody webhookRequest: WebhookRequest): Mono<out Any> {
        val forwarderConfig = runBlocking { persistence.get("forwarderInfo", typeRef<List<Forwarder.ForwarderConfig>>()) }
        val config = forwarderConfig?.firstOrNull { it.token == webhookRequest.token }
        if (config == null) {
            return Mono.just("failed")
        }
        return Mono.fromCallable { apiRequest.postChannelsMessages(config.channelId, webhookRequest.text) }

            .onErrorResume(ApiRequestFailure::class.java) {
                log.debug("ApiRequestFailure error", it)
                if (it.errorData?.code == 304023) {
                    val data = it.errorData.data
                    data as HashMap<*, *>
                    val auditId = (data["message_audit"] as HashMap<*, *>)["audit_id"] as String
                    log.warn("Message auditing, auditId: {}", auditId)
                }
                Mono.empty()
            }.map {
                if (it != null) {
                    "success"
                } else "failed"
            }.defaultIfEmpty("failed").doOnEach { log.info("result: {}", it.get()) }
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
