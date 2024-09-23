package ng.i.sav.qdroid.log

import org.slf4j.ILoggerFactory
import org.slf4j.Logger

class LoggerFactoryImpl : ILoggerFactory {

    private val loggerMap = HashMap<String, Logger>()
    private val defaultLogger = LoggerImpl()

    init {
        loggerMap[""] = defaultLogger
    }

    override fun getLogger(name: String?): Logger {
        return if (name == null) {
            defaultLogger
        } else {
            val actualName = name.removeSuffix("\$Companion")
            loggerMap[actualName] ?: LoggerImpl(actualName).apply { loggerMap[actualName] = this }
        }
    }
}
