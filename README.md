# qdroid-qq-bot

A client for qq guild bot. 一个QQ频道机器人接口的kotlin实现.
基于[QQ官方机器人文档](https://bot.q.qq.com/wiki/)开发, 使用kotlin语言

## 开发

使用prompt加速开发, 修改 `待转换内容：`后的内容即可

### 对象创建prompt:

```text
按照下列规律将内容转换为kt代码，文档注释和类属性的变量名改为小驼峰，需加@JsonProperty注解
内容：
MessageMarkdown
字段名	类型	描述
template_id	int	markdown 模板 id
custom_template_id	string	markdown 自定义模板 id
params	MessageMarkdownParams	markdown 模板模板参数
content	string	原生 markdown 内容,与上面三个参数互斥,参数都传值将报错。
代码：
/**
 * @property templateId    int	markdown 模板 id
 * @property customTemplateId    string	markdown 自定义模板 id
 * @property params    [MessageMarkdownParams]	markdown 模板模板参数
 * @property content    string	原生 markdown 内容,与上面三个参数互斥,参数都传值将报错。
 * */
data class MessageMarkdown(
    @JsonProperty("template_id")
    val templateId: Int,
    @JsonProperty("custom_template_id")
    val customTemplateId: Stringl,
    @JsonProperty("params")
    val params: MessageMarkdownParams,
    @JsonProperty("content")
    val content: String
)
待转换内容：
AudioAction
字段名	类型	描述
guild_id	string	频道id
channel_id	string	子频道id
audio_url	string	音频数据的url status为0时传
text	string	状态文本（比如：简单爱-周杰伦），可选，status为0时传，其他操作不传
```

### 枚举创建prompt:

```text
按照下列规律将内容转换为kt代码
内容：
PrivateType
值	描述
0	公开频道
1	群主管理员可见
2	群主管理员+指定成员，可使用 修改子频道权限接口 指定成员
代码：
/**
 * @property PUBLIC 0	公开频道
 * @property PRIVATE 1	群主管理员可见
 * @property PROTECTED 2	群主管理员+指定成员，可使用 修改子频道权限接口 指定成员
 * */
@JsonDeserialize(using = PrivateType.Deserializer::class)
@JsonSerialize(using = PrivateType.Serializer::class)
enum class PrivateType(
    val type: Int,
    val typeName: String,
) {
    PUBLIC(0, "公开频道"),
    PRIVATE(1, "群主管理员可见"),
    PROTECTED(2, "群主管理员+指定成员"),
    UNKNOWN(-1, "未知类型");

    class Deserializer : JsonDeserializer<PrivateType>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): PrivateType {
            val i = p.valueAsInt
            return PrivateType.entries.find { it.type == i } ?: UNKNOWN
        }

    }

    class Serializer : JsonSerializer<PrivateType>() {
        override fun serialize(value: PrivateType, gen: JsonGenerator, serializers: SerializerProvider?) {
            gen.writeNumber(value.type)
        }
    }
}
待转换内容：
STATUS
字段名	值	描述
START	0	开始播放操作
PAUSE	1	暂停播放操作
RESUME	2	继续播放操作
STOP	3	停止播放操作
```

## 特别鸣谢 (Special Thanks)

[![JetBrains](https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.svg)](https://www.jetbrains.com/idea/)
