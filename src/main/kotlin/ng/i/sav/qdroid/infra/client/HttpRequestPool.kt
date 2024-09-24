package ng.i.sav.qdroid.infra.client

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.future.future
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import org.springframework.beans.factory.annotation.Autowired
import java.time.Duration
import java.util.concurrent.*
import kotlin.coroutines.resume

class HttpRequestPool(
    private var maxConcurrent: Int = 1,
    @Autowired(required = false)
    private var threadFactory: ThreadFactory? = null
) {
    private lateinit var executorService: ExecutorService

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

    suspend fun <T> runAsync(timeout: Duration = Duration.ofSeconds(60), block: () -> T): T {
        return suspendCancellableCoroutine { continuation ->
            executorService.execute {
                runBlocking {
                    withTimeout(timeout.toMillis()) {
                        val result = block()
                        continuation.resume(result)
                    }
                }
            }
        }
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
