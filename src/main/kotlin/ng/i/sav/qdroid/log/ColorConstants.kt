package ng.i.sav.qdroid.log

/**
 * 颜色对应有点问题, 没时间调先用着
 * */
class ColorConstants {
    companion object {
        const val ESC_START = "\u001b["
        const val ESC_END = "m"
        const val BOLD = "1;"
        const val BLACK_FG = "30"
        const val RED_FG = "31"
        const val GREEN_FG = "32"
        const val YELLOW_FG = "33"
        const val BLUE_FG = "34"
        const val MAGENTA_FG = "35"
        const val CYAN_FG = "36"
        const val WHITE_FG = "37"
        const val DEFAULT_FG = "39"
        const val BLACK_BG = "40"
        const val RED_BG = "41"
        const val GREEN_BG = "42"
        const val YELLOW_BG = "43"
        const val BLUE_BG = "44"
        const val MAGENTA_BG = "45"
        const val CYAN_BG = "46"
        const val WHITE_BG = "47"
        const val DEFAULT_BG = "49"

        fun getColorPattern(color: String): String {
            return "$ESC_START$color$ESC_END"
        }

        fun getColorPattern(foregroundColor: String, backgroundColor: String): String {
            return "$ESC_START$foregroundColor;$backgroundColor$ESC_END"
        }

        fun getColorPattern(bold: String, foregroundColor: String, backgroundColor: String): String {
            return "$ESC_START$bold$foregroundColor;$backgroundColor$ESC_END"
        }

        fun getDefault(): String {
            return "$ESC_START$ESC_END"
        }
    }
}
