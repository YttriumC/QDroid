package ng.i.sav.qdroid.bot.util

import ng.i.sav.qdroid.infra.model.Message

object MessageUtils {
    fun String.toBuilder(): StringBuilder {
        return StringBuilder(this)
    }

    fun removeMentions(message: Message): String {
        var msgStr = message.content ?: return ""
        val ids = message.mentions?.map { it.id } ?: return msgStr
        ids.forEach {
            msgStr = msgStr.replace("<@!$it>\\s?".toRegex(), "")
        }
        return msgStr
    }
}