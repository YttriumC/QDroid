package ng.i.sav.qdroid.infra.client

class BotRequestFailure : RuntimeException {
    constructor(msg: String) : super(msg)
    constructor() : super()
    constructor(msg: String, cause: Throwable) : super(msg, cause)
    constructor(cause: Throwable) : super(cause)

    companion object {
        fun toPath(path: String): BotRequestFailure {
            return BotRequestFailure("Failed to proceed request: '$path'.")
        }
    }
}
