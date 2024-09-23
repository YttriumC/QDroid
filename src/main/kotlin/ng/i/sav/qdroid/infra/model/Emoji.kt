package ng.i.sav.qdroid.infra.model

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize

/**
 * @property id    string	表情ID，系统表情使用数字为ID，emoji使用emoji本身为id，参考 Emoji 列表
 * @property type    uint32	表情类型 [EmojiType]
 * */
class Emoji(val id: String, val type: EmojiType = EmojiType.EMOJI) {

    @Suppress("NonAsciiCharacters", "UNUSED")
    companion object {

        val 得意 = Emoji("4", EmojiType.BUILTIN)

        val 流泪 = Emoji("5", EmojiType.BUILTIN)

        val 睡 = Emoji("8", EmojiType.BUILTIN)

        val 大哭 = Emoji("9", EmojiType.BUILTIN)

        val 尴尬 = Emoji("10", EmojiType.BUILTIN)

        val 调皮 = Emoji("12", EmojiType.BUILTIN)

        val 微笑 = Emoji("14", EmojiType.BUILTIN)

        val 酷 = Emoji("16", EmojiType.BUILTIN)

        val 可爱 = Emoji("21", EmojiType.BUILTIN)

        val 傲慢 = Emoji("23", EmojiType.BUILTIN)

        val 饥饿 = Emoji("24", EmojiType.BUILTIN)

        val 困 = Emoji("25", EmojiType.BUILTIN)

        val 惊恐 = Emoji("26", EmojiType.BUILTIN)

        val 流汗 = Emoji("27", EmojiType.BUILTIN)

        val 憨笑 = Emoji("28", EmojiType.BUILTIN)

        val 悠闲 = Emoji("29", EmojiType.BUILTIN)

        val 奋斗 = Emoji("30", EmojiType.BUILTIN)

        val 疑问 = Emoji("32", EmojiType.BUILTIN)

        val 嘘 = Emoji("33", EmojiType.BUILTIN)

        val 晕 = Emoji("34", EmojiType.BUILTIN)

        val 敲打 = Emoji("38", EmojiType.BUILTIN)

        val 再见 = Emoji("39", EmojiType.BUILTIN)

        val 发抖 = Emoji("41", EmojiType.BUILTIN)

        val 爱情 = Emoji("42", EmojiType.BUILTIN)

        val 跳跳 = Emoji("43", EmojiType.BUILTIN)

        val 拥抱 = Emoji("49", EmojiType.BUILTIN)

        val 蛋糕 = Emoji("53", EmojiType.BUILTIN)

        val 咖啡 = Emoji("60", EmojiType.BUILTIN)

        val 玫瑰 = Emoji("63", EmojiType.BUILTIN)

        val 爱心 = Emoji("66", EmojiType.BUILTIN)

        val 太阳 = Emoji("74", EmojiType.BUILTIN)

        val 月亮 = Emoji("75", EmojiType.BUILTIN)

        val 赞 = Emoji("76", EmojiType.BUILTIN)

        val 握手 = Emoji("78", EmojiType.BUILTIN)

        val 胜利 = Emoji("79", EmojiType.BUILTIN)

        val 飞吻 = Emoji("85", EmojiType.BUILTIN)

        val 西瓜 = Emoji("89", EmojiType.BUILTIN)

        val 冷汗 = Emoji("96", EmojiType.BUILTIN)

        val 擦汗 = Emoji("97", EmojiType.BUILTIN)

        val 抠鼻 = Emoji("98", EmojiType.BUILTIN)

        val 鼓掌 = Emoji("99", EmojiType.BUILTIN)

        val 糗大了 = Emoji("100", EmojiType.BUILTIN)

        val 坏笑 = Emoji("101", EmojiType.BUILTIN)

        val 左哼哼 = Emoji("102", EmojiType.BUILTIN)

        val 右哼哼 = Emoji("103", EmojiType.BUILTIN)

        val 哈欠 = Emoji("104", EmojiType.BUILTIN)

        val 委屈 = Emoji("106", EmojiType.BUILTIN)

        val 左亲亲 = Emoji("109", EmojiType.BUILTIN)

        val 可怜 = Emoji("111", EmojiType.BUILTIN)

        val 示爱 = Emoji("116", EmojiType.BUILTIN)

        val 抱拳 = Emoji("118", EmojiType.BUILTIN)

        val 拳头 = Emoji("120", EmojiType.BUILTIN)

        val 爱你 = Emoji("122", EmojiType.BUILTIN)

        val NO = Emoji("123", EmojiType.BUILTIN)

        val OK = Emoji("124", EmojiType.BUILTIN)

        val 转圈 = Emoji("125", EmojiType.BUILTIN)

        val 挥手 = Emoji("129", EmojiType.BUILTIN)

        val 喝彩 = Emoji("144", EmojiType.BUILTIN)

        val 棒棒糖 = Emoji("147", EmojiType.BUILTIN)

        val 茶 = Emoji("171", EmojiType.BUILTIN)

        val 泪奔 = Emoji("173", EmojiType.BUILTIN)

        val 无奈 = Emoji("174", EmojiType.BUILTIN)

        val 卖萌 = Emoji("175", EmojiType.BUILTIN)

        val 小纠结 = Emoji("176", EmojiType.BUILTIN)

        val doge = Emoji("179", EmojiType.BUILTIN)

        val 惊喜 = Emoji("180", EmojiType.BUILTIN)

        val 骚扰 = Emoji("181", EmojiType.BUILTIN)

        val 笑哭 = Emoji("182", EmojiType.BUILTIN)

        val 我最美 = Emoji("183", EmojiType.BUILTIN)

        val 点赞 = Emoji("201", EmojiType.BUILTIN)

        val 托脸 = Emoji("203", EmojiType.BUILTIN)

        val 托腮 = Emoji("212", EmojiType.BUILTIN)

        val 啵啵 = Emoji("214", EmojiType.BUILTIN)

        val 蹭一蹭 = Emoji("219", EmojiType.BUILTIN)

        val 抱抱 = Emoji("222", EmojiType.BUILTIN)

        val 拍手 = Emoji("227", EmojiType.BUILTIN)

        val 佛系 = Emoji("232", EmojiType.BUILTIN)

        val 喷脸 = Emoji("240", EmojiType.BUILTIN)

        val 甩头 = Emoji("243", EmojiType.BUILTIN)

        val 加油抱抱 = Emoji("246", EmojiType.BUILTIN)

        val 脑阔疼 = Emoji("262", EmojiType.BUILTIN)

        val 捂脸 = Emoji("264", EmojiType.BUILTIN)

        val 辣眼睛 = Emoji("265", EmojiType.BUILTIN)

        val 哦哟 = Emoji("266", EmojiType.BUILTIN)

        val 头秃 = Emoji("267", EmojiType.BUILTIN)

        val 问号脸 = Emoji("268", EmojiType.BUILTIN)

        val 暗中观察 = Emoji("269", EmojiType.BUILTIN)

        val emm = Emoji("270", EmojiType.BUILTIN)

        val 吃瓜 = Emoji("271", EmojiType.BUILTIN)

        val 呵呵哒 = Emoji("272", EmojiType.BUILTIN)

        val 我酸了 = Emoji("273", EmojiType.BUILTIN)

        val 汪汪 = Emoji("277", EmojiType.BUILTIN)

        val 汗 = Emoji("278", EmojiType.BUILTIN)

        val 无眼笑 = Emoji("281", EmojiType.BUILTIN)

        val 敬礼 = Emoji("282", EmojiType.BUILTIN)

        val 面无表情 = Emoji("284", EmojiType.BUILTIN)

        val 摸鱼 = Emoji("285", EmojiType.BUILTIN)

        val 哦 = Emoji("287", EmojiType.BUILTIN)

        val 睁眼 = Emoji("289", EmojiType.BUILTIN)

        val 敲开心 = Emoji("290", EmojiType.BUILTIN)

        val 摸锦鲤 = Emoji("293", EmojiType.BUILTIN)

        val 期待 = Emoji("294", EmojiType.BUILTIN)

        val 拜谢 = Emoji("297", EmojiType.BUILTIN)

        val 元宝 = Emoji("298", EmojiType.BUILTIN)

        val 牛啊 = Emoji("299", EmojiType.BUILTIN)

        val 右亲亲 = Emoji("305", EmojiType.BUILTIN)

        val 牛气冲天 = Emoji("306", EmojiType.BUILTIN)

        val 喵喵 = Emoji("307", EmojiType.BUILTIN)

        val 仔细分析 = Emoji("314", EmojiType.BUILTIN)

        val 加油 = Emoji("315", EmojiType.BUILTIN)

        val 崇拜 = Emoji("318", EmojiType.BUILTIN)

        val 比心 = Emoji("319", EmojiType.BUILTIN)

        val 庆祝 = Emoji("320", EmojiType.BUILTIN)

        val 拒绝 = Emoji("322", EmojiType.BUILTIN)

        val 吃糖 = Emoji("324", EmojiType.BUILTIN)

        val 生气 = Emoji("326", EmojiType.BUILTIN)
    }
}

/**
 * 1	系统表情
 * 2	emoji表情
 * */
@JsonDeserialize(using = EmojiType.Deserializer::class)
@JsonSerialize(using = EmojiType.Serializer::class)
enum class EmojiType(val type: Int, val desc: String) {
    BUILTIN(1, "系统表情"),
    EMOJI(2, "emoji表情"),
    UNKNOWN(-1, "未知类型");

    class Deserializer : JsonDeserializer<EmojiType>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): EmojiType {
            val i = p.valueAsInt
            return EmojiType.entries.find { it.type == i } ?: UNKNOWN
        }

    }

    class Serializer : JsonSerializer<EmojiType>() {
        override fun serialize(value: EmojiType, gen: JsonGenerator, serializers: SerializerProvider?) {
            gen.writeNumber(value.type)
        }
    }
}
