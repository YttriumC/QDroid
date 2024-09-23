package ng.i.sav.log

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level

object Slf4kt {
    fun getLogger(clazz: Class<*>): Logger {
        return LoggerFactory.getLogger(clazz)
    }

    fun getLogger(name: String): Logger {
        return LoggerFactory.getLogger(name)
    }

    fun setLevel(level: Level) {
        LoggerImpl.level = level
    }

    fun getLevel(): Level = LoggerImpl.level
}


