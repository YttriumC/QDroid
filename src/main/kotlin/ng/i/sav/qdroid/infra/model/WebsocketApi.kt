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
 * @property url    String    WebSocket 的连接地址
 * @property shards    Int    建议的 shard 数
 * @property sessionStartLimit    SessionStartLimit    创建 Session 限制信息
 */
data class WebsocketApi(
    @JsonProperty("url")
    val url: String,
    @JsonProperty("shards")
    val shards: Int,
    @JsonProperty("session_start_limit")
    val sessionStartLimit: SessionStartLimit
)


/**
 * @property total Int 每 24 小时可创建 Session 数
 * @property remaining Int 目前还可以创建的 Session 数
 * @property resetAfter Int 重置计数的剩余时间(ms)
 * @property maxConcurrency Int 每 5s 可以创建的 Session 数
 */
data class SessionStartLimit(
    @JsonProperty("total")
    val total: Int,
    @JsonProperty("remaining")
    val remaining: Int,
    @JsonProperty("reset_after")
    val resetAfter: Int,
    @JsonProperty("max_concurrency")
    val maxConcurrency: Int
)


/**
 * @property op 指的是 opcode，全部 opcode 列表参考 opcode。
 *
 * @property s 下行消息都会有一个序列号，标识消息的唯一性，客户端需要再发送心跳的时候，携带客户端收到的最新的s。
 *
 * @property t 和 d 主要是用在op为 0 Dispatch 的时候，t 代表事件类型，d 代表事件内容，不同事件类型的事件内容格式都不同，请注意识别。
 * */

/*@JsonSubTypes(
    JsonSubTypes.Type(Op10Hello::class, name = "10")
)*/

data class Payload<T>(
    @JsonProperty("op")
    val op: OpCode,
    /*    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "op"
        )*/
    @JsonProperty("d")
    val d: T?,
    @JsonProperty("s")
    val s: Int?,
    @JsonProperty("t")
    val t: String? = null,
    @JsonProperty("id")
    val id: String? = null,
) {

    companion object {
        fun <T> with(opCode: OpCode, d: T? = null, s: Int? = null, t: String? = null): Payload<T> {
            return Payload(opCode, d, s, t)
        }
    }

    override fun toString(): String {
        return "Payload(op=$op, d=$d, s=$s, t=$t${id?.let { ", id=$id" } ?: ""})"
    }
}

/**
 * @property heartbeatInterval 心跳间隔
 * */
data class Op10Hello(
    @JsonProperty("heartbeat_interval")
    val heartbeatInterval: Int
)

/**
 *
 * */
data class Op6Resume(
    @JsonProperty("token")
    val token: String,
    @JsonProperty("session_id")
    val sessionId: String,
    @JsonProperty("seq")
    val seq: Int
)

/**
 *
 * */
data class ReadyEvent(
    @JsonProperty("version")
    val version: Int,
    @JsonProperty("session_id")
    val sessionId: String,
    @JsonProperty("user")
    val user: User,
    @JsonProperty("shard")
    val shard: List<Int>,
)

/**
 * OpCode
 *
 * 所有opcode列表如下：
 *
 * CODE	名称	客户端操作	描述
 * 0	Dispatch	Receive	服务端进行消息推送
 * 1	Heartbeat	Send/Receive	客户端或服务端发送心跳
 * 2	Identify	Send	客户端发送鉴权
 * 6	Resume	Send	客户端恢复连接
 * 7	Reconnect	Receive	服务端通知客户端重新连接
 * 9	Invalid Session	Receive	当identify或resume的时候，如果参数有错，服务端会返回该消息
 * 10	Hello	Receive	当客户端与网关建立ws连接之后，网关下发的第一条消息
 * 11	Heartbeat ACK	Receive/Reply	当发送心跳成功之后，就会收到该消息
 * 12	HTTP Callback ACK	Reply	仅用于 http 回调模式的回包，代表机器人收到了平台推送的数据
 * 客户端操作含义如下：
 *
 * Receive 客户端接收到服务端 push 的消息
 * Send 客户端发送消息
 * Reply 客户端接收到服务端发送的消息之后的回包（HTTP 回调模式）
 * */
@JsonDeserialize(using = OpCode.Deserializer::class)
@JsonSerialize(using = OpCode.Serializer::class)
enum class OpCode(
    val code: Int,
    val codeName: String,
) {
    DISPATCH(0, "服务端进行消息推送"),
    HEARTBEAT(1, "客户端或服务端发送心跳"),
    IDENTIFY(2, "客户端发送鉴权"),
    RESUME(6, "客户端恢复连接"),
    RECONNECT(7, "服务端通知客户端重新连接"),
    INVALID_SESSION(9, "当identify或resume的时候，如果参数有错，服务端会返回该消息"),
    HELLO(10, "当客户端与网关建立ws连接之后，网关下发的第一条消息"),
    HEARTBEAT_ACK(11, "当发送心跳成功之后，就会收到该消息"),
    HTTP_CALLBACK_ACK(12, "仅用于 http 回调模式的回包，代表机器人收到了平台推送的数据"),
    UNKNOWN(-1, "未知消息");

    class Deserializer : JsonDeserializer<OpCode>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): OpCode {
            val i = p.valueAsInt
            return OpCode.entries.find { it.code == i } ?: UNKNOWN
        }

    }

    class Serializer : JsonSerializer<OpCode>() {
        override fun serialize(value: OpCode, gen: JsonGenerator, serializers: SerializerProvider?) {
            gen.writeNumber(value.code)
        }
    }
}
