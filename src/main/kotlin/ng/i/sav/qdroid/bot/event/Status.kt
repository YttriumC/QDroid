package ng.i.sav.qdroid.bot.event

import ng.i.sav.qdroid.infra.client.ApiRequest
import ng.i.sav.qdroid.infra.model.Message
import java.lang.management.ManagementFactory


class Status : MessageInstruction {
    override fun getInstructions(): List<String> = listOf("!status", "/status")

    override fun execute(
        apiRequest: ApiRequest,
        addInterceptor: (userId: String, interceptExecutor: MessageInterceptorManager.InterceptExecutor) -> Unit,
        remainContent: String?,
        message: Message,
        eventId: String?
    ) {
        val os = ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean::class.java)
        val gcType = ManagementFactory.getGarbageCollectorMXBeans().firstOrNull { it.isValid }?.name ?: ""
        apiRequest.postChannelsMessages(
            message.channelId, msgId = message.id,
            content = /*"Start time: ${DateTimeFormat.LOCAL_DATE_TIME_FORMATTER.format(botManager.startTime)} \n" +*/
            "Used memory: ${
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576
            }MB \n" +
                    "GC type: $gcType\n" +
                    "CPU load: ${"%.2f".format(os.cpuLoad)}\n" +
                    "OS Version: ${os.name}_${os.version}_${os.arch}"
        )
    }

    override fun getDomain(): String {
        return "inst.status"
    }

}
