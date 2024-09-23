package ng.i.sav.bot.qdroid.client

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.*

@Component
class HttpRequestPool {
    private lateinit var executorService: ExecutorService

    @Autowired(required = false)
    private var threadFactory: ThreadFactory? = null

    @Value("")
    private var maxConcurrent: Int = 1

    fun setMaxConcurrent(maxConcurrent: Int) {
        this.maxConcurrent = maxConcurrent
    }

    fun getExecutorService(): ExecutorService = executorService

    @PostConstruct
    fun init() {
        executorService = if (threadFactory == null)
            ThreadPoolExecutor(
                1, maxConcurrent,
                60, TimeUnit.SECONDS, LinkedBlockingDeque(1 shl 10)
            )
        else
            ThreadPoolExecutor(
                1, maxConcurrent,
                60, TimeUnit.SECONDS, LinkedBlockingDeque(1 shl 10),
                threadFactory!!
            )
    }

    fun <T> runSync(timeout: Duration = Duration.ofSeconds(60), block: () -> T): T {
        val future = executorService.submit<T> { return@submit block() }
        return future.get(timeout.seconds, TimeUnit.SECONDS)
    }

    fun <T> runAsync(timeout: Duration = Duration.ofSeconds(60), block: Callable<T>): Future<T> {
        return executorService.submit(block)
    }

    fun <T> runAsync(
        timeout: Duration = Duration.ofSeconds(60),
        callable: Callable<T>,
        callback: ((T?, Throwable?) -> Unit)? = null
    ) {
        val wrapper: Callable<Unit> = Callable<Unit> {
            var ex: Throwable? = null
            var r: T? = null
            try {
                r = callable.call()
            } catch (e: Throwable) {
                ex = e
            } finally {
                if (callback != null)
                    callback(r, ex)
            }
        }
        executorService.invokeAll(listOf(wrapper), timeout.seconds, TimeUnit.SECONDS)
    }
}
