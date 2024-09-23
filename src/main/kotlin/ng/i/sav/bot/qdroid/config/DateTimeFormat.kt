package ng.i.sav.bot.qdroid.config

import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter

class DateTimeFormat {

    companion object {
        const val DATE_TIME_WITH_MILLIS_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss.SSS"
        const val DATE_TIME_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss"
        const val TIME_FORMAT_STRING = "HH:mm:ss.SSS"
        const val DATE_FORMAT_STRING = "yyyy-MM-dd"
        val DATE_TIME_WITH_MILLIS_FORMATTER = SimpleDateFormat(DATE_TIME_WITH_MILLIS_FORMAT_STRING)
        val LOCAL_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT_STRING)
    }
}
