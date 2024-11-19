package ng.i.sav.qdroid.bot.event

import jakarta.annotation.PostConstruct
import ng.i.sav.qdroid.bot.util.MessageUtils
import ng.i.sav.qdroid.infra.client.ApiRequest
import ng.i.sav.qdroid.infra.client.MessageEventHandler
import ng.i.sav.qdroid.infra.model.Message
import ng.i.sav.qdroid.infra.model.Payload
import ng.i.sav.qdroid.log.Slf4kt
import org.springframework.stereotype.Component

@Component
class MessageInstructManager(
    private val messageInstructions: List<MessageInstruction>,
    private val messageInterceptorManager: MessageInterceptorManager
) :
    MessageEventHandler {
    private val executorMap: Map<String, List<MessageInstruction>> =
        messageInstructions.flatMap { it.getInstructions().map { i -> i to it } }
            .groupByTo(mutableMapOf(), { it.first }, { it.second })
    private val instructionSet = executorMap.keys
    override suspend fun onEvent(apiRequest: ApiRequest, event: Message, payload: Payload<Message>) {
        if (messageInterceptorManager.onEvent(apiRequest, payload)) {
            return
        }
        event.content?.let {
            val msg = MessageUtils.removeMentions(event).trimStart { it.isWhitespace() || it == '\t' }
            msg.split(' ', '\t', limit = 2).takeIf { it.isNotEmpty() }?.let { inst ->
                if (instructionSet.contains(inst[0])) {
                    executorMap[inst[0]]?.forEach {
                        try {
                            it.execute(
                                apiRequest,
                                { u, i -> messageInterceptorManager.addInterceptUser(u, i) },
                                inst.getOrNull(1),
                                event,
                                payload.id
                            )
                        } catch (e: Exception) {
                            log.warn("指令 {} 执行异常", it.getInstructions(), e)
                        }
                    }
                }
            }
        }
    }

    @PostConstruct
    fun info() {
        messageInstructions.forEach {
            log.info("Instruction '{}' loaded", it.getInstructions())
        }
    }

    companion object {
        private val log = Slf4kt.getLogger(MessageInstructManager::class.java)
    }
}
