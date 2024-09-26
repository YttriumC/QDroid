package ng.i.sav.qdroid.infra.client

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.annotation.JsonSerialize
import ng.i.sav.qdroid.infra.model.*
import ng.i.sav.qdroid.infra.model.event.*

/**
 * [Payload.t] 事件类型与对应实体类型关系
 * */
@JsonSerialize(using = Event.Serializer::class)
@JsonDeserialize(using = Event.Deserializer::class)
enum class Event(val desc: String, val type: Class<*>) {
    READY("READY event", ReadyEvent::class.java),


    GUILD_CREATE("当机器人加入新guild时", GuildEvent::class.java),
    GUILD_UPDATE("当guild资料发生变更时", GuildEvent::class.java),
    GUILD_DELETE("当机器人退出guild时", GuildEvent::class.java),
    CHANNEL_CREATE("当channel被创建时", ChannelEvent::class.java),
    CHANNEL_UPDATE("当channel被更新时", ChannelEvent::class.java),
    CHANNEL_DELETE("当channel被删除时", ChannelEvent::class.java),

    GUILD_MEMBER_ADD("当成员加入时", GuildMemberEvent::class.java),
    GUILD_MEMBER_UPDATE("当成员资料变更时", GuildMemberEvent::class.java),
    GUILD_MEMBER_REMOVE("当成员被移除时", GuildMemberEvent::class.java),

    MESSAGE_CREATE(
        "发送消息事件，代表频道内的全部消息，而不只是 at 机器人的消息。内容与 AT_MESSAGE_CREATE 相同",
        Message::class.java
    ),
    MESSAGE_DELETE("删除（撤回）消息事件", MessageDeleteEvent::class.java),

    MESSAGE_REACTION_ADD("为消息添加表情表态", MessageReaction::class.java),
    MESSAGE_REACTION_REMOVE("为消息删除表情表态", MessageReaction::class.java),

    DIRECT_MESSAGE_CREATE("当收到用户发给机器人的私信消息时", Message::class.java),
    DIRECT_MESSAGE_DELETE("删除（撤回）消息事件", MessageDeleteEvent::class.java),

    OPEN_FORUM_THREAD_CREATE("当用户创建主题时", ForumEvent::class.java),
    OPEN_FORUM_THREAD_UPDATE("当用户更新主题时", ForumEvent::class.java),
    OPEN_FORUM_THREAD_DELETE("当用户删除主题时", ForumEvent::class.java),
    OPEN_FORUM_POST_CREATE("当用户创建帖子时", ForumEvent::class.java),
    OPEN_FORUM_POST_DELETE("当用户删除帖子时", ForumEvent::class.java),
    OPEN_FORUM_REPLY_CREATE("当用户回复评论时", ForumEvent::class.java),
    OPEN_FORUM_REPLY_DELETE("当用户删除评论时", ForumEvent::class.java),

    AUDIO_OR_LIVE_CHANNEL_MEMBER_ENTER("当用户进入音视频/直播子频道", LiveChannelEvent::class.java),
    AUDIO_OR_LIVE_CHANNEL_MEMBER_EXIT("当用户离开音视频/直播子频道", LiveChannelEvent::class.java),

    INTERACTION_CREATE("互动事件创建时", InteractiveEvent::class.java),

    MESSAGE_AUDIT_PASS("消息审核通过", MessageAudited::class.java),
    MESSAGE_AUDIT_REJECT("消息审核不通过", MessageAudited::class.java),

    FORUM_THREAD_CREATE("当用户创建主题时", Thread::class.java),
    FORUM_THREAD_UPDATE("当用户更新主题时", Thread::class.java),
    FORUM_THREAD_DELETE("当用户删除主题时", Thread::class.java),
    FORUM_POST_CREATE("当用户创建帖子时", Post::class.java),
    FORUM_POST_DELETE("当用户删除帖子时", Post::class.java),
    FORUM_REPLY_CREATE("当用户回复评论时", Reply::class.java),
    FORUM_REPLY_DELETE("当用户删除评论时", Reply::class.java),
    FORUM_PUBLISH_AUDIT_RESULT("当用户发表审核通过时", MessageAudited::class.java),

    AUDIO_START("音频开始播放时", AudioAction::class.java),
    AUDIO_FINISH("音频播放结束时", AudioAction::class.java),
    AUDIO_ON_MIC("上麦时", AudioAction::class.java),
    AUDIO_OFF_MIC("下麦时", AudioAction::class.java),

    AT_MESSAGE_CREATE("当收到@机器人的消息时", Message::class.java),
    PUBLIC_MESSAGE_DELETE("当频道的消息被删除时", MessageDeleteEvent::class.java),
    ;


    class Deserializer : JsonDeserializer<Event>() {
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Event {
            val i = p.valueAsString
            return Event.valueOf(i)
        }

    }

    class Serializer : JsonSerializer<Event>() {
        override fun serialize(value: Event, gen: JsonGenerator, serializers: SerializerProvider?) {
            gen.writeString(value.name)
        }
    }
}
