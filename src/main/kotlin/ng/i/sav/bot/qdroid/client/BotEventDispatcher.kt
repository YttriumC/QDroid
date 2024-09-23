package ng.i.sav.bot.qdroid.client

import ng.i.sav.bot.qdroid.model.Payload
import ng.i.sav.bot.qdroid.util.toObj
import ng.i.sav.log.Slf4kt
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class BotEventDispatcher(private val handlers: List<BotEventHandler<*>>, private val objectMapper: ObjectMapper) {
    fun onEvent(bot: ng.i.sav.bot.qdroid.client.GuildBot, event: String) {
        log.info("Dispatcher msg: {}", event)
        val payload = objectMapper.toObj<Payload<Any>>(event)

        when (Event.valueOf(payload.t!!)) {
            Event.GUILD_CREATE -> {

                handlers.forEach { handler ->
                    if (handler is GuildCreateHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.GUILD_UPDATE -> {
                handlers.forEach { handler ->
                    if (handler is GuildUpdateHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.GUILD_DELETE -> {
                handlers.forEach { handler ->
                    if (handler is GuildDeleteHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.CHANNEL_CREATE -> {
                handlers.forEach { handler ->
                    if (handler is ChannelCreateHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.CHANNEL_UPDATE -> {
                handlers.forEach { handler ->
                    if (handler is ChannelUpdateHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.CHANNEL_DELETE -> {
                handlers.forEach { handler ->
                    if (handler is ChannelDeleteHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.GUILD_MEMBER_ADD -> {
                handlers.forEach { handler ->
                    if (handler is GuildMemberAddHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.GUILD_MEMBER_UPDATE -> {
                handlers.forEach { handler ->
                    if (handler is GuildMemberUpdateHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.GUILD_MEMBER_REMOVE -> {
                handlers.forEach { handler ->
                    if (handler is GuildMemberRemoveHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.MESSAGE_CREATE -> {
                handlers.forEach { handler ->
                    if (handler is AtMessageCreateHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.MESSAGE_DELETE -> {
                log.info("MESSAGE_DELETE: {}", event)
            }


            Event.MESSAGE_REACTION_ADD -> {
                handlers.forEach { handler ->
                    if (handler is MessageReactionAddHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.MESSAGE_REACTION_REMOVE -> {
                handlers.forEach { handler ->
                    if (handler is MessageReactionRemoveHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.DIRECT_MESSAGE_CREATE -> {
                handlers.forEach { handler ->
                    if (handler is DirectMessageCreateHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.DIRECT_MESSAGE_DELETE -> {
                log.info("DIRECT_MESSAGE_DELETE: {}", event)
            }


            Event.OPEN_FORUM_THREAD_CREATE -> {
                handlers.forEach { handler ->
                    if (handler is OpenForumThreadCreateHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.OPEN_FORUM_THREAD_UPDATE -> {
                handlers.forEach { handler ->
                    if (handler is OpenForumThreadUpdateHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.OPEN_FORUM_THREAD_DELETE -> {
                handlers.forEach { handler ->
                    if (handler is OpenForumThreadDeleteHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.OPEN_FORUM_POST_CREATE -> {
                handlers.forEach { handler ->
                    if (handler is OpenForumPostCreateHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.OPEN_FORUM_POST_DELETE -> {
                handlers.forEach { handler ->
                    if (handler is OpenForumPostDeleteHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.OPEN_FORUM_REPLY_CREATE -> {
                handlers.forEach { handler ->
                    if (handler is OpenForumReplyCreateHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.OPEN_FORUM_REPLY_DELETE -> {
                handlers.forEach { handler ->
                    if (handler is OpenForumReplyDeleteHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.AUDIO_OR_LIVE_CHANNEL_MEMBER_ENTER -> {
                handlers.forEach { handler ->
                    if (handler is AudioOrLiveChannelMemberEnterHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.AUDIO_OR_LIVE_CHANNEL_MEMBER_EXIT -> {
                handlers.forEach { handler ->
                    if (handler is AudioOrLiveChannelMemberExitHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.INTERACTION_CREATE -> {}


            Event.MESSAGE_AUDIT_PASS -> {
                handlers.forEach { handler ->
                    if (handler is MessageAuditPassHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.MESSAGE_AUDIT_REJECT -> {
                handlers.forEach { handler ->
                    if (handler is MessageAuditRejectHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.FORUM_THREAD_CREATE -> {
                handlers.forEach { handler ->
                    if (handler is ForumThreadCreateHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.FORUM_THREAD_UPDATE -> {
                handlers.forEach { handler ->
                    if (handler is ForumThreadUpdateHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.FORUM_THREAD_DELETE -> {
                handlers.forEach { handler ->
                    if (handler is ForumThreadDeleteHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.FORUM_POST_CREATE -> {
                handlers.forEach { handler ->
                    if (handler is ForumPostCreateHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.FORUM_POST_DELETE -> {
                handlers.forEach { handler ->
                    if (handler is ForumPostDeleteHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.FORUM_REPLY_CREATE -> {
                handlers.forEach { handler ->
                    if (handler is ForumReplyCreateHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.FORUM_REPLY_DELETE -> {
                handlers.forEach { handler ->
                    if (handler is ForumReplyDeleteHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.FORUM_PUBLISH_AUDIT_RESULT -> {
                handlers.forEach { handler ->
                    if (handler is ForumPublishAuditResultHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.AUDIO_START -> {
                handlers.forEach { handler ->
                    if (handler is AudioStartHandler) handler.onEvent(bot, objectMapper.toObj(payload.d!!), payload.t)
                }
            }

            Event.AUDIO_FINISH -> {
                handlers.forEach { handler ->
                    if (handler is AudioFinishHandler) handler.onEvent(bot, objectMapper.toObj(payload.d!!), payload.t)
                }
            }

            Event.AUDIO_ON_MIC -> {
                handlers.forEach { handler ->
                    if (handler is AudioOnMicHandler) handler.onEvent(bot, objectMapper.toObj(payload.d!!), payload.t)
                }
            }

            Event.AUDIO_OFF_MIC -> {
                handlers.forEach { handler ->
                    if (handler is AudioOffMicHandler) handler.onEvent(bot, objectMapper.toObj(payload.d!!), payload.t)
                }
            }

            Event.AT_MESSAGE_CREATE -> {
                handlers.forEach { handler ->
                    if (handler is AtMessageCreateHandler) handler.onEvent(
                        bot,
                        objectMapper.toObj(payload.d!!),
                        payload.t
                    )
                }
            }

            Event.PUBLIC_MESSAGE_DELETE -> {}
        }

    }

    companion object {
        private val log = Slf4kt.getLogger(BotEventDispatcher::class.java)
    }
}

enum class Event(desc: String) {
    GUILD_CREATE("当机器人加入新guild时"),
    GUILD_UPDATE("当guild资料发生变更时"),
    GUILD_DELETE("当机器人退出guild时"),
    CHANNEL_CREATE("当channel被创建时"),
    CHANNEL_UPDATE("当channel被更新时"),
    CHANNEL_DELETE("当channel被删除时"),

    GUILD_MEMBER_ADD("当成员加入时"),
    GUILD_MEMBER_UPDATE("当成员资料变更时"),
    GUILD_MEMBER_REMOVE("当成员被移除时"),

    MESSAGE_CREATE("发送消息事件，代表频道内的全部消息，而不只是 at 机器人的消息。内容与 AT_MESSAGE_CREATE 相同"),
    MESSAGE_DELETE("删除（撤回）消息事件"),

    MESSAGE_REACTION_ADD("为消息添加表情表态"),
    MESSAGE_REACTION_REMOVE("为消息删除表情表态"),

    DIRECT_MESSAGE_CREATE("当收到用户发给机器人的私信消息时"),
    DIRECT_MESSAGE_DELETE("删除（撤回）消息事件"),

    OPEN_FORUM_THREAD_CREATE("当用户创建主题时"),
    OPEN_FORUM_THREAD_UPDATE("当用户更新主题时"),
    OPEN_FORUM_THREAD_DELETE("当用户删除主题时"),
    OPEN_FORUM_POST_CREATE("当用户创建帖子时"),
    OPEN_FORUM_POST_DELETE("当用户删除帖子时"),
    OPEN_FORUM_REPLY_CREATE("当用户回复评论时"),
    OPEN_FORUM_REPLY_DELETE("当用户删除评论时"),

    AUDIO_OR_LIVE_CHANNEL_MEMBER_ENTER("当用户进入音视频/直播子频道"),
    AUDIO_OR_LIVE_CHANNEL_MEMBER_EXIT("当用户离开音视频/直播子频道"),

    INTERACTION_CREATE("互动事件创建时"),

    MESSAGE_AUDIT_PASS("消息审核通过"),
    MESSAGE_AUDIT_REJECT("消息审核不通过"),

    FORUM_THREAD_CREATE("当用户创建主题时"),
    FORUM_THREAD_UPDATE("当用户更新主题时"),
    FORUM_THREAD_DELETE("当用户删除主题时"),
    FORUM_POST_CREATE("当用户创建帖子时"),
    FORUM_POST_DELETE("当用户删除帖子时"),
    FORUM_REPLY_CREATE("当用户回复评论时"),
    FORUM_REPLY_DELETE("当用户删除评论时"),
    FORUM_PUBLISH_AUDIT_RESULT("当用户发表审核通过时"),

    AUDIO_START("音频开始播放时"),
    AUDIO_FINISH("音频播放结束时"),
    AUDIO_ON_MIC("上麦时"),
    AUDIO_OFF_MIC("下麦时"),

    AT_MESSAGE_CREATE("当收到@机器人的消息时"),
    PUBLIC_MESSAGE_DELETE("当频道的消息被删除时"),


}
