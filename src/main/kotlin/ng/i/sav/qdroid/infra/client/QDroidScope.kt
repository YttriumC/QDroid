package ng.i.sav.qdroid.infra.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException

object QDroidScope : CoroutineScope {
    private val job = Job()
    override val coroutineContext: CoroutineContext = Dispatchers.Default + job

    fun stop( cause: CancellationException?) {
        job.cancel(cause)
    }

    init {
        Runtime.getRuntime().addShutdownHook(Thread{ stop(CancellationException("Program exit.")) })
    }
}
