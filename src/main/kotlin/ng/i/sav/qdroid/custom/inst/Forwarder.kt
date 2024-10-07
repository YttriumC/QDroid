package ng.i.sav.qdroid.custom.inst

import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.coroutines.runBlocking
import ng.i.sav.qdroid.bot.event.MessageInstruction
import ng.i.sav.qdroid.bot.event.MessageInterceptorManager
import ng.i.sav.qdroid.bot.event.NamedPersistenceModule
import ng.i.sav.qdroid.infra.client.ApiPath
import ng.i.sav.qdroid.infra.client.ApiRequest
import ng.i.sav.qdroid.infra.model.Message
import ng.i.sav.qdroid.infra.model.sendText
import ng.i.sav.qdroid.infra.util.typeRef
import org.springframework.stereotype.Component

@Component
class Forwarder(namedPersistenceModule: NamedPersistenceModule) : MessageInstruction {
    private val persistence = namedPersistenceModule.getPersistence(this)
    override fun getInstructions(): List<String> = arrayListOf("!forwarder", "/forwarder")

    override fun execute(
        apiRequest: ApiRequest,
        addInterceptor: (userId: String, interceptExecutor: MessageInterceptorManager.InterceptExecutor) -> Unit,
        remainContent: String?,
        message: Message,
        eventId: String?
    ) {

        var forwarderConfigs = runBlocking {
            persistence.get(
                "forwarderInfo",
                typeRef<ArrayList<ForwarderConfig>>()
            )
        }
        var token = forwarderConfigs?.firstOrNull { message.channelId == it.channelId && message.author.id == it.userId }?.token
        if (token == null) {
            token = randomToken()
            if (forwarderConfigs == null) {
                forwarderConfigs = arrayListOf(ForwarderConfig(token, message.channelId, message.author.id))
            } else {
                forwarderConfigs.add(ForwarderConfig(token, message.channelId, message.author.id))
            }
            runBlocking { persistence.save("forwarderInfo", forwarderConfigs) }
        }
        message.sendText(apiRequest, "token: $token")
    }

    /**
     * Random character from a-z, A-Z, 0-9, length 18
     * */
    fun randomToken(): String {
        val chars = "abc1def2ghi3jkl4mno5pqr6stu7vwx8yzA9BCD0EFGHIJKLMNOPQRSTUVWXYZ"
        return (1..18).map { chars.random() }.joinToString("")
    }

    override fun getDomain(): String = "forwarder"

    data class ForwarderConfig(
        @JsonProperty("token") val token: String,
        @JsonProperty("channelId") val channelId: String,
        @JsonProperty("userId") val userId: String
    )
}
