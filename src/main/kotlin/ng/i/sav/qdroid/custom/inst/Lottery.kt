package ng.i.sav.qdroid.custom.inst

import ng.i.sav.qdroid.bot.event.MessageInstruction
import ng.i.sav.qdroid.bot.event.MessageInterceptorManager
import ng.i.sav.qdroid.infra.client.ApiRequest
import ng.i.sav.qdroid.infra.model.Message
import ng.i.sav.qdroid.infra.model.sendText
import ng.i.sav.qdroid.log.Slf4kt
import org.springframework.stereotype.Component

@Component
class Lottery : MessageInstruction {
    override fun getInstructions(): List<String> = listOf("!彩票", "彩票")

    override fun execute(
        apiRequest: ApiRequest,
        addInterceptor: (userId: String, interceptExecutor: MessageInterceptorManager.InterceptExecutor) -> Unit,
        remainContent: String?,
        message: Message,
        eventId: String?
    ) {
        log.info("User {} 生成彩票", message.author.id)
        val ssqRedBall = hashSetOf(
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
            17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33
        )
        val ssqBlueBall = hashSetOf(
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
        )
        val lottery = remainContent?.trim()
        if (lottery == "双色球")
            return message.sendText(
                apiRequest, "${lottery}随机序列: \n" +
                        "红球: ${ssqRedBall.takeOne()} ${ssqRedBall.takeOne()} ${ssqRedBall.takeOne()}" +
                        " ${ssqRedBall.takeOne()} ${ssqRedBall.takeOne()} ${ssqRedBall.takeOne()} \n" +
                        "蓝球: ${ssqBlueBall.random()}\n" +
                        "免责声明: 以上内容仅供娱乐, 并非推荐购买"
            )
        val dltRedBall = hashSetOf(
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16,
            17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35
        )
        val dltBlueBall = hashSetOf(
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12,
        )
        if (lottery == "大乐透")
            return message.sendText(
                apiRequest, "${lottery}随机序列: \n" +
                        "红球: ${dltRedBall.takeOne()} ${dltRedBall.takeOne()} ${dltRedBall.takeOne()}" +
                        " ${dltRedBall.takeOne()} ${dltRedBall.takeOne()}\n" +
                        "蓝球: ${dltBlueBall.takeOne()} ${dltBlueBall.takeOne()}\n" +
                        "免责声明: 以上内容仅供娱乐, 并非推荐购买"
            )
        return message.sendText(
            apiRequest, "使用方法: !彩票 [彩票类型]\n" +
                    "目前支持双色球或大乐透"
        )
    }

    override fun getDomain(): String {
        return "inst.lottery"
    }

    private fun HashSet<Int>.takeOne(): Int {
        return this.random().also { this.remove(it) }
    }

    companion object {
        private val log = Slf4kt.getLogger(Lottery::class.java)
    }
}
