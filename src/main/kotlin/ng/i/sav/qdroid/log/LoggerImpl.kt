package ng.i.sav.qdroid.log

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.Marker
import org.slf4j.event.Level
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.PrintStream
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.LinkedBlockingQueue


class LoggerImpl(private val name: String) : Logger {

    private val shortenedName: String

    companion object {
        private val log = LoggerFactory.getLogger(LoggerImpl::class.java)
        private const val LOGGER_NAME_LENGTH = 40
        private const val THREAD_NAME_MIN_LENGTH = 20
        private const val LOGGER_BUFFER_CAPACITY = 8192

        @Volatile
        var level: Level = Level.INFO

        val defaultLogStream: PrintStream get() = System.out

        @Volatile
        private var logStream = defaultLogStream

        private val logQueue = LinkedBlockingQueue<CharSequence>(LOGGER_BUFFER_CAPACITY)

        private val dateFormatter = ng.i.sav.qdroid.infra.config.DateTimeFormat.DATE_TIME_WITH_MILLIS_FORMATTER

        fun setLogStream(printStream: PrintStream) {
            log.info(
                "Logger stream setting to: {}", if (printStream === System.out) "System.out" else printStream.toString()
            )
            logStream = if (printStream.checkError()) {
                System.out
            } else {
                if (logStream !== System.out) {
                    logStream.close()
                }
                printStream
            }
        }

        fun checkFileValidation(file: File): Boolean {
            if (!file.exists()) {
                file.parentFile.mkdirs()
                file.createNewFile()
            }
            return file.exists() && file.canWrite()
        }

        init {
            Thread {
                while (true) {
                    try {
                        while (true) {
                            val take = logQueue.take()
                            logStream.println(take)
                        }
                    } catch (_: IOException) {
                        logStream = defaultLogStream
                        log.error("IOException occurred, fallback to System.out")
                    } catch (_: Exception) {
                    }
                }
            }.let {
                it.name = "logging-daemon"
                it.isDaemon = true
                it.start()
            }
        }

        private fun CharSequence?.replaceFirstLogPlaceHolder(`var`: Any?): StringBuilder {
            if (this == null) return StringBuilder()
            val sb = if (this is StringBuilder) {
                this
            } else {
                StringBuilder(this)
            }
            val index = sb.indexOf("{}")
            if (index != -1) {
                sb.replace(index, index + 2, `var`.toString())
            }
            return sb
        }

        fun setLoggerOutputFile(file: File): Boolean {
            if (checkFileValidation(file)) {
                return try {
                    log.info("Logger output setting to: '{}'", file)
                    logStream = (PrintStream(FileOutputStream(file, true), true, StandardCharsets.UTF_8))
                    true
                } catch (e: Exception) {
                    log.warn("Exception occurred on change logger output file: {}, fallback to System.out", file)
                    logStream = defaultLogStream
                    false
                }
            }
            return false
        }
    }

    constructor() : this("Default")

    init {
        shortenedName = shortenClassName()
    }

    private fun shortenClassName(): String {
        try {
            val strings = name.split(".")
            val sb = StringBuilder()
            var shortenedLength = name.length
            for ((i, s) in strings.withIndex()) {
                if (i == strings.lastIndex) break
                if (shortenedLength <= LOGGER_NAME_LENGTH) {
                    for (j in i until strings.lastIndex) {
                        sb.append(strings[j]).append('.')
                    }
                    break
                }
                sb.append(s[0]).append('.')
                shortenedLength -= (s.length - 1)
            }
            sb.append(strings.last())
            return sb.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return name
        } finally {
        }
    }

    override fun getName(): String {
        return name
    }

    private fun formatThreadName(): CharSequence {
        val s = Thread.currentThread().name
        if (s.length <= THREAD_NAME_MIN_LENGTH) return String.format("%${THREAD_NAME_MIN_LENGTH}s", s)
        return "...${s.substring(s.length - THREAD_NAME_MIN_LENGTH + 3)}"
    }

    override fun trace(msg: String?, t: Throwable?) {
        if (isTraceEnabled) logQueue.offer(
            "[${ColorConstants.getColorPattern(ColorConstants.WHITE_FG)}${
                String.format(
                    "%-5s",
                    Level.TRACE
                )
            }${ColorConstants.getDefault()}] ${
                dateFormatter.format(Date())
            } [${formatThreadName()}] ${
                String.format("%-${LOGGER_NAME_LENGTH}s", shortenedName)
            }: ${msg.toString()}${if (t == null) "" else "\n${t.stackTraceToString()}"}"
        )
    }

    override fun trace(marker: Marker?, msg: String?, t: Throwable?) {
        if (isTraceEnabled) logQueue.offer(
            "[${ColorConstants.getColorPattern(ColorConstants.WHITE_FG)}${
                String.format(
                    "%-5s",
                    Level.TRACE
                )
            }${ColorConstants.getDefault()}] ${
                dateFormatter.format(Date())
            }|${marker ?: ""} [${formatThreadName()}] ${
                String.format("%-${LOGGER_NAME_LENGTH}s", shortenedName)
            }: ${msg.toString()}${if (t == null) "" else "\n${t.stackTraceToString()}"}"
        )
    }

    override fun debug(msg: String?, t: Throwable?) {
        if (isDebugEnabled) logQueue.offer(
            "[${ColorConstants.getColorPattern(ColorConstants.BLUE_FG)}${
                String.format(
                    "%-5s",
                    Level.DEBUG
                )
            }${ColorConstants.getDefault()}] ${
                dateFormatter.format(Date())
            } [${formatThreadName()}] ${
                String.format("%-${LOGGER_NAME_LENGTH}s", shortenedName)
            }: ${msg.toString()}${if (t == null) "" else "\n${t.stackTraceToString()}"}"
        )
    }

    override fun debug(marker: Marker?, msg: String?, t: Throwable?) {
        if (isDebugEnabled) logQueue.offer(
            "[${ColorConstants.getColorPattern(ColorConstants.BLUE_FG)}${
                String.format(
                    "%-5s",
                    Level.DEBUG
                )
            }${ColorConstants.getDefault()}] ${
                dateFormatter.format(Date())
            }|${marker ?: ""} [${formatThreadName()}] ${
                String.format("%-${LOGGER_NAME_LENGTH}s", shortenedName)
            }: ${msg.toString()}${if (t == null) "" else "\n${t.stackTraceToString()}"}"
        )
    }

    override fun info(msg: String?, t: Throwable?) {
        if (isInfoEnabled) logQueue.offer(
            "[${ColorConstants.getColorPattern(ColorConstants.CYAN_FG)}${
                String.format(
                    "%-5s",
                    Level.INFO
                )
            }${ColorConstants.getDefault()}] ${
                dateFormatter.format(Date())
            } [${formatThreadName()}] ${
                String.format("%-${LOGGER_NAME_LENGTH}s", shortenedName)
            }: ${msg.toString()}${if (t == null) "" else "\n${t.stackTraceToString()}"}"
        )
    }

    override fun info(marker: Marker?, msg: String?, t: Throwable?) {
        logQueue.offer(
            "[${ColorConstants.getColorPattern(ColorConstants.CYAN_FG)}${
                String.format(
                    "%-5s",
                    Level.INFO
                )
            }${ColorConstants.getDefault()}] ${
                dateFormatter.format(Date())
            }|${marker ?: ""} [${formatThreadName()}] ${
                String.format("%-${LOGGER_NAME_LENGTH}s", shortenedName)
            }: ${msg.toString()}${if (t == null) "" else "\n${t.stackTraceToString()}"}"
        )
    }

    override fun warn(msg: String?, t: Throwable?) {
        if (isWarnEnabled) logQueue.offer(
            "[${ColorConstants.getColorPattern(ColorConstants.YELLOW_FG)}${
                String.format(
                    "%-5s",
                    Level.WARN
                )
            }${ColorConstants.getDefault()}] ${
                dateFormatter.format(Date())
            } [${formatThreadName()}] ${
                String.format("%-${LOGGER_NAME_LENGTH}s", shortenedName)
            }: ${msg.toString()}${if (t == null) "" else "\n${t.stackTraceToString()}"}"
        )
    }

    override fun warn(marker: Marker?, msg: String?, t: Throwable?) {
        if (isWarnEnabled) logQueue.offer(
            "[${ColorConstants.getColorPattern(ColorConstants.YELLOW_FG)}${
                String.format(
                    "%-5s",
                    Level.WARN
                )
            }${ColorConstants.getDefault()}] ${
                dateFormatter.format(Date())
            }|${marker ?: ""} [${formatThreadName()}] ${
                String.format("%-${LOGGER_NAME_LENGTH}s", shortenedName)
            }: ${msg.toString()}${if (t == null) "" else "\n${t.stackTraceToString()}"}"
        )
    }

    override fun error(msg: String?, t: Throwable?) {
        if (isErrorEnabled) logQueue.offer(
            "[${ColorConstants.getColorPattern(ColorConstants.RED_FG)}${
                String.format(
                    "%-5s",
                    Level.ERROR
                )
            }${ColorConstants.getDefault()}] ${
                dateFormatter.format(Date())
            } [${formatThreadName()}] ${
                String.format("%-${LOGGER_NAME_LENGTH}s", shortenedName)
            }: ${msg.toString()}${if (t == null) "" else "\n${t.stackTraceToString()}"}"
        )
    }

    override fun error(marker: Marker?, msg: String?, t: Throwable?) {
        if (isErrorEnabled) logQueue.offer(
            "[${ColorConstants.getColorPattern(ColorConstants.RED_FG)}${
                String.format(
                    "%-5s",
                    Level.ERROR
                )
            }${ColorConstants.getDefault()}] ${
                dateFormatter.format(Date())
            }|${marker ?: ""} [${formatThreadName()}] ${
                String.format("%-${LOGGER_NAME_LENGTH}s", shortenedName)
            }: ${msg.toString()}${if (t == null) "" else "\n${t.stackTraceToString()}"}"
        )
    }

    override fun isTraceEnabled(): Boolean {
        return Level.TRACE.toInt() >= level.toInt()
    }

    override fun isTraceEnabled(marker: Marker?): Boolean {
        return Level.TRACE.toInt() >= level.toInt()
    }

    override fun trace(msg: String?) {
        if (isTraceEnabled) trace(msg, null)
    }

    override fun trace(format: String?, arg: Any?) {
        if (isTraceEnabled) trace(format.replaceFirstLogPlaceHolder(arg).toString(), null)
    }

    override fun trace(format: String?, arg1: Any?, arg2: Any?) {
        if (isTraceEnabled) trace(
            format.replaceFirstLogPlaceHolder(arg1).replaceFirstLogPlaceHolder(arg2).toString(), null
        )
    }

    override fun trace(format: String?, vararg arguments: Any?) {
        if (isTraceEnabled && format != null) {
            var sb = StringBuilder(format)
            arguments.forEach { sb = sb.replaceFirstLogPlaceHolder(it) }
            trace(sb.toString())
        }
    }


    override fun trace(marker: Marker?, msg: String?) {
        if (isTraceEnabled) trace(marker, msg, null)
    }

    override fun trace(marker: Marker?, format: String?, arg: Any?) {
        if (isTraceEnabled) trace(marker, format.replaceFirstLogPlaceHolder(arg).toString())
    }

    override fun trace(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        if (isTraceEnabled) trace(
            marker, format.replaceFirstLogPlaceHolder(arg1).replaceFirstLogPlaceHolder(arg2).toString()
        )
    }

    override fun trace(marker: Marker?, format: String?, vararg arguments: Any?) {
        if (isTraceEnabled && format != null) {
            var sb = StringBuilder()
            arguments.forEach { sb = sb.replaceFirstLogPlaceHolder(it) }
            trace(marker, sb.toString())
        }
    }


    override fun isDebugEnabled(): Boolean {
        return Level.DEBUG.toInt() >= level.toInt()
    }

    override fun isDebugEnabled(marker: Marker?): Boolean {
        return Level.DEBUG.toInt() >= level.toInt()
    }

    override fun debug(msg: String?) {
        if (isDebugEnabled) debug(msg, null)
    }

    override fun debug(format: String?, arg: Any?) {
        if (isDebugEnabled) debug(format.replaceFirstLogPlaceHolder(arg).toString())

    }

    override fun debug(format: String?, arg1: Any?, arg2: Any?) {
        if (isDebugEnabled) debug(
            format.replaceFirstLogPlaceHolder(arg1).replaceFirstLogPlaceHolder(arg2).toString(), null
        )
    }

    override fun debug(format: String?, vararg arguments: Any?) {
        if (isDebugEnabled && format != null) {
            var sb = StringBuilder(format)
            arguments.forEach { sb = sb.replaceFirstLogPlaceHolder(it) }
            debug(sb.toString())
        }
    }

    override fun debug(marker: Marker?, msg: String?) {
        if (isDebugEnabled) debug(marker, msg, null)
    }

    override fun debug(marker: Marker?, format: String?, arg: Any?) {
        if (isDebugEnabled) debug(marker, format.replaceFirstLogPlaceHolder(arg).toString())
    }

    override fun debug(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        if (isDebugEnabled) debug(
            marker, format.replaceFirstLogPlaceHolder(arg1).replaceFirstLogPlaceHolder(arg2).toString()
        )
    }

    override fun debug(marker: Marker?, format: String?, vararg arguments: Any?) {
        if (isDebugEnabled && format != null) {
            var sb = StringBuilder()
            arguments.forEach { sb = sb.replaceFirstLogPlaceHolder(it) }
            debug(marker, sb.toString())
        }
    }

    override fun isInfoEnabled(): Boolean {
        return Level.INFO.toInt() >= level.toInt()
    }

    override fun isInfoEnabled(marker: Marker?): Boolean {
        return Level.INFO.toInt() >= level.toInt()
    }

    override fun info(msg: String?) {
        if (isInfoEnabled) info(msg, null)
    }

    override fun info(format: String?, arg: Any?) {
        if (isInfoEnabled) info(format.replaceFirstLogPlaceHolder(arg).toString())
    }

    override fun info(format: String?, arg1: Any?, arg2: Any?) {
        if (isInfoEnabled) info(
            format.replaceFirstLogPlaceHolder(arg1).replaceFirstLogPlaceHolder(arg2).toString(), null
        )
    }

    override fun info(format: String?, vararg arguments: Any?) {
        if (isInfoEnabled && format != null) {
            var sb = StringBuilder(format)
            arguments.forEach { sb = sb.replaceFirstLogPlaceHolder(it) }
            info(sb.toString())
        }
    }


    override fun info(marker: Marker?, msg: String?) {
        if (isInfoEnabled) info(marker, msg, null)
    }

    override fun info(marker: Marker?, format: String?, arg: Any?) {
        if (isInfoEnabled) info(marker, format, *arrayOf(arg))
    }

    override fun info(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        if (isInfoEnabled) debug(marker, format, *arrayOf(arg1, arg2))
    }

    override fun info(marker: Marker?, format: String?, vararg arguments: Any?) {
        if (isInfoEnabled && format != null) {
            var sb = StringBuilder()
            arguments.forEach { sb = sb.replaceFirstLogPlaceHolder(it) }
            info(marker, sb.toString())
        }
    }


    override fun isWarnEnabled(): Boolean {
        return Level.WARN.toInt() >= level.toInt()
    }

    override fun isWarnEnabled(marker: Marker?): Boolean {
        return Level.WARN.toInt() >= level.toInt()
    }

    override fun warn(msg: String?) {
        if (isWarnEnabled) warn(msg, null)
    }

    override fun warn(format: String?, arg: Any?) {
        if (isWarnEnabled) warn(format.replaceFirstLogPlaceHolder(arg).toString())
    }

    override fun warn(format: String?, arg1: Any?, arg2: Any?) {
        if (isWarnEnabled) warn(format, *arrayOf(arg1, arg2))
    }


    override fun warn(format: String?, vararg arguments: Any?) {
        if (isWarnEnabled && format != null) {
            var sb = StringBuilder(format)
            arguments.forEach { sb = sb.replaceFirstLogPlaceHolder(it) }
            warn(sb.toString())
        }
    }

    override fun warn(marker: Marker?, msg: String?) {
        if (isWarnEnabled) warn(marker, msg, null)
    }

    override fun warn(marker: Marker?, format: String?, arg: Any?) {
        if (isWarnEnabled) warn(marker, format.replaceFirstLogPlaceHolder(arg).toString())
    }

    override fun warn(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        if (isWarnEnabled) warn(marker, format, *arrayOf(arg1, arg2))
    }

    override fun warn(marker: Marker?, format: String?, vararg arguments: Any?) {
        if (isWarnEnabled && format != null) {
            var sb = StringBuilder()
            arguments.forEach { sb = sb.replaceFirstLogPlaceHolder(it) }
            warn(marker, sb.toString())
        }
    }


    override fun isErrorEnabled(): Boolean {
        return Level.ERROR.toInt() >= level.toInt()
    }

    override fun isErrorEnabled(marker: Marker?): Boolean {
        return Level.ERROR.toInt() >= level.toInt()
    }

    override fun error(msg: String?) {
        if (isErrorEnabled) error(msg, null)
    }

    override fun error(format: String?, arg: Any?) {
        if (isErrorEnabled) error(format.replaceFirstLogPlaceHolder(arg).toString())
    }

    override fun error(format: String?, arg1: Any?, arg2: Any?) {
        if (isErrorEnabled) error(format, *arrayOf(arg1, arg2))
    }

    override fun error(format: String?, vararg arguments: Any?) {
        if (isErrorEnabled && format != null) {
            var sb = StringBuilder(format)
            arguments.forEach { sb = sb.replaceFirstLogPlaceHolder(it) }
            error(sb.toString())
        }
    }


    override fun error(marker: Marker?, msg: String?) {
        if (isErrorEnabled) error(marker, msg, null)
    }

    override fun error(marker: Marker?, format: String?, arg: Any?) {
        if (isErrorEnabled) error(marker, format.replaceFirstLogPlaceHolder(arg).toString())
    }

    override fun error(marker: Marker?, format: String?, arg1: Any?, arg2: Any?) {
        if (isErrorEnabled) error(marker, format, *arrayOf(arg1, arg2))
    }

    override fun error(marker: Marker?, format: String?, vararg arguments: Any?) {
        if (isErrorEnabled && format != null) {
            var sb = StringBuilder()
            arguments.forEach { sb = sb.replaceFirstLogPlaceHolder(it) }
            error(marker, sb.toString())
        }
    }

}
