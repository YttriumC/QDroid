package ng.i.sav.qdroid.infra.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize

/**
 * @property id    string	按钮 id
 * @property renderData    [RenderData] 按纽渲染展示对象	用于设定按钮的显示效果
 * @property action    [Action] 该按纽操作相关字段	用于设定按钮点击后的操作
 * */
data class Button(
    @JsonProperty("id")
    val id: String,
    @JsonProperty("render_data")
    val renderData: RenderData,
    @JsonProperty("action")
    val action: Action
)

/**
 * @property label    string	按纽上的文字
 * @property visitedLabel    string	点击后按纽上文字
 * @property style    int	按钮样式，参考 [RenderStyle]
 * */
data class RenderData(
    @JsonProperty("label")
    val label: String,
    @JsonProperty("visited_label")
    val visitedLabel: String,
    @JsonProperty("style")
    val style: RenderStyle
)

/**
 * 0	int	灰色线框
 * 1	int	蓝色线框
 * */
@JsonDeserialize(using = RenderStyle.Deserializer::class)
@JsonSerialize(using = RenderStyle.Serializer::class)
enum class RenderStyle(
    val style: Int,
    val styleName: String,
) {
    GRAY(0, "灰色线框"),
    BLUE(1, "蓝色线框"),
    UNKNOWN(-1, "未知类型");


    class Deserializer : JsonDeserializer<RenderStyle>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): RenderStyle {
            val i = p.valueAsInt
            return RenderStyle.entries.find { it.style == i } ?: UNKNOWN
        }

    }

    class Serializer : JsonSerializer<RenderStyle>() {
        override fun serialize(value: RenderStyle, gen: JsonGenerator, serializers: SerializerProvider?) {
            gen.writeNumber(value.style)
        }
    }
}

/**
 * @property type    int	操作类型，参考 [ActionType]
 * @property permission    [Permission] 对象	用于设定操作按钮所需的权限
 * @property clickLimit    int	可点击的次数, 默认不限
 * @property data    string	操作相关数据
 * @property atBotShowChannelList    bool	false:不弹出子频道选择器 true:弹出子频道选择器
 * */
data class Action(
    @JsonProperty("type")
    val type: Int,
    @JsonProperty("permission")
    val permission: Permission,
    @JsonProperty("click_limit")
    val clickLimit: Int,
    @JsonProperty("unsupport_tips")
    val unsupportTips: String?,
    @JsonProperty("data")
    val data: String,
    @JsonProperty("at_bot_show_channel_list")
    val atBotShowChannelList: Boolean
)

/**
 * 0	http 或 小程序 客户端识别 schem, data字段为链接
 * 1	回调后台接口, data 传给后台
 * 2	at机器人, 根据 at_bot_show_channel_list 决定在当前频道或用户选择频道,自动在输入框 @bot data
 * */
@JsonDeserialize(using = ActionType.Deserializer::class)
@JsonSerialize(using = ActionType.Serializer::class)
enum class ActionType(
    val type: Int,
    val typeName: String,
) {
    LINK(0, "http 或 小程序 客户端识别 schem, data字段为链接"),
    CALLBACK(1, "回调后台接口, data 传给后台"),
    BOT(2, "at机器人"),
    UNKNOWN(-1, "未知类型");

    class Deserializer : JsonDeserializer<ActionType>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ActionType {
            val i = p.valueAsInt
            return ActionType.entries.find { it.type == i } ?: UNKNOWN
        }

    }

    class Serializer : JsonSerializer<ActionType>() {
        override fun serialize(value: ActionType, gen: JsonGenerator, serializers: SerializerProvider?) {
            gen.writeNumber(value.type)
        }
    }
}

/**
 * 0	指定用户可操作
 * 1	仅管理者可操作
 * 2	所有人可操作
 * 3	指定身份组可操作
 * */
@JsonDeserialize(using = PermissionType.Deserializer::class)
@JsonSerialize(using = PermissionType.Serializer::class)
enum class PermissionType(
    val type: Int,
    val typeName: String,
) {
    USER(0, "指定用户可操作"),
    ADMIN(1, "仅管理者可操作"),
    ALL(2, "所有人可操作"),
    ROLE(3, "指定身份组可操作"),
    UNKNOWN(-1, "未知类型");

    class Deserializer : JsonDeserializer<PermissionType>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): PermissionType {
            val i = p.valueAsInt
            return PermissionType.entries.find { it.type == i } ?: UNKNOWN
        }

    }

    class Serializer : JsonSerializer<PermissionType>() {
        override fun serialize(value: PermissionType, gen: JsonGenerator, serializers: SerializerProvider?) {
            gen.writeNumber(value.type)
        }
    }
}

/**
 * @property type    int	权限类型，参考 [PermissionType]
 * @property specifyRoleIds    string 数组	有权限的身份组id的列表
 * @property specifyUserIds    string 数组	有权限的用户id的列表
 * */
data class Permission(
    @JsonProperty("type")
    val type: PermissionType,
    @JsonProperty("specify_role_ids")
    val specifyRoleIds: List<String>?,
    @JsonProperty("specify_user_ids")
    val specifyUserIds: List<String>?,
)

/**
 * @property buttons    [Button] 按钮对象数组	数组的一项代表一个按钮，每个 [InlineKeyboardRow] 最多含有 5 个 Button
 * */
data class InlineKeyboardRow(
    @JsonProperty("buttons")
    val buttons: List<Button>
)

/**
 * @property rows    [InlineKeyboardRow] 消息按钮组件行对象数组	数组的一项代表消息按钮组件的一行,最多含有 5 行
 * */
data class InlineKeyboard(
    @JsonProperty("rows")
    val rows: List<InlineKeyboardRow>,
    @JsonProperty("bot_appid")
    val botAppId: Int
)
