package ng.i.sav.qdroid.bot.event

import ng.i.sav.qdroid.infra.client.QDroid
import ng.i.sav.qdroid.infra.config.DateTimeFormat
import ng.i.sav.qdroid.infra.model.Message
import java.lang.management.ManagementFactory


class Status : MessageInstruction {
    override fun getInstructions(): List<String> = listOf("!status", "/status")

    override fun execute(
        bot: QDroid,
        addInterceptor: (userId: String, interceptExecutor: MessageInterceptorManager.InterceptExecutor) -> Unit,
        remainContent: String?,
        event: Message,
        eventId: String?
    ) {
        val os = ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean::class.java)
        val gcType = ManagementFactory.getGarbageCollectorMXBeans().firstOrNull { it.isValid }?.name ?: ""
        bot.postChannelsMessages(
            event.channelId, msgId = event.id,
            content = "Start time: ${DateTimeFormat.LOCAL_DATE_TIME_FORMATTER.format(bot.startTime)} \n" +
                    "Used memory: ${
                        (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576
                    }MB \n" +
                    "GC type: $gcType\n" +
                    "CPU load: ${"%.2f".format(os.cpuLoad)}\n" +
                    "OS Version: ${os.name}_${os.version}_${os.arch}"
        )
    }

}
