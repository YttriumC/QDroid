package ng.i.sav.qdroid.infra.client

object ErrorCodeMap : HashMap<Int, Pair<String, Class<*>>>() {
    private fun readResolve(): Any = ErrorCodeMap

    init {
        this[304022] = "PUSH_TIME 推送消息时间限制" to Unit::class.java
        this[304023] = "PUSH_MSG_ASYNC_OK 推送消息异步调用成功, 等待人工审核" to HashMap::class.java
    }
}
