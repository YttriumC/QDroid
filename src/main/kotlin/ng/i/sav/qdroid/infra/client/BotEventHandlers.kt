package ng.i.sav.qdroid.infra.client

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import ng.i.sav.qdroid.infra.model.*
import ng.i.sav.qdroid.infra.model.event.*
import ng.i.sav.qdroid.log.Slf4kt


interface BotEventHandler<T> {
    val acceptEvents get() = arrayOf<Event>()

    fun onEvent(bot: QDroid, payload: Payload<*>) {
        if (payload.d == null) {
            log.warn("Event {} data is null", payload.t)
        } else
            @Suppress("UNCHECKED_CAST")
            onEvent(bot, payload.d as T, payload as Payload<T>)
    }

    fun onEvent(bot: QDroid, event: T, payload: Payload<T>)

    @PostConstruct
    fun checkAcceptEvent() {
        if (acceptEvents.distinctBy { it.type }.size > 1) throw IllegalStateException("acceptEvents type must be unique")
    }

    companion object {
        private val log = Slf4kt.getLogger(BotEventHandler::class.java)
    }
}

interface MessageDeleteHandler : BotEventHandler<MessageDeleteEvent> {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.PUBLIC_MESSAGE_DELETE, Event.DIRECT_MESSAGE_DELETE)
}

interface GuildEventHandler : BotEventHandler<GuildEvent> {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.GUILD_CREATE, Event.GUILD_UPDATE, Event.GUILD_DELETE)
}

interface ChannelEventHandler : BotEventHandler<ChannelEvent> {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.CHANNEL_CREATE, Event.CHANNEL_UPDATE, Event.CHANNEL_DELETE)
}

interface MemberEventHandler : BotEventHandler<GuildMemberEvent> {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.GUILD_MEMBER_ADD, Event.GUILD_MEMBER_UPDATE, Event.GUILD_MEMBER_REMOVE)
}

interface MessageEventHandler : BotEventHandler<Message> {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.AT_MESSAGE_CREATE, Event.DIRECT_MESSAGE_CREATE)
}

interface MessageAuditEventHandler : BotEventHandler<MessageAudited> {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.MESSAGE_AUDIT_PASS, Event.MESSAGE_AUDIT_REJECT)
}

interface MessageReactionEventHandler : BotEventHandler<MessageReaction> {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.MESSAGE_REACTION_ADD, Event.MESSAGE_REACTION_REMOVE)
}

interface AudioEventHandler : BotEventHandler<AudioAction> {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.AUDIO_START, Event.AUDIO_FINISH, Event.AUDIO_ON_MIC, Event.AUDIO_OFF_MIC)
}

interface ForumThreadEventHandler : BotEventHandler<Thread> {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.FORUM_THREAD_CREATE, Event.FORUM_THREAD_UPDATE, Event.FORUM_THREAD_DELETE)
}

interface ForumPostEventHandler : BotEventHandler<Post> {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.FORUM_POST_CREATE, Event.FORUM_POST_DELETE)
}

interface ForumReplyEventHandler : BotEventHandler<Reply> {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.FORUM_REPLY_CREATE, Event.FORUM_REPLY_DELETE)
}

interface OpenForumEventHandler : BotEventHandler<ForumEvent> {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.OPEN_FORUM_THREAD_CREATE, Event.OPEN_FORUM_THREAD_UPDATE, Event.OPEN_FORUM_THREAD_DELETE)
}

interface LiveChannelEventHandler : BotEventHandler<LiveChannelEvent> {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.AUDIO_OR_LIVE_CHANNEL_MEMBER_ENTER, Event.AUDIO_OR_LIVE_CHANNEL_MEMBER_EXIT)
}

interface GuildCreateHandler : GuildEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.GUILD_CREATE)
}

interface GuildUpdateHandler : GuildEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.GUILD_UPDATE)
}

interface GuildDeleteHandler : GuildEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.GUILD_DELETE)
}

interface ChannelCreateHandler : ChannelEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.CHANNEL_CREATE)
}

interface ChannelUpdateHandler : ChannelEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.CHANNEL_UPDATE)
}

interface ChannelDeleteHandler : ChannelEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.CHANNEL_DELETE)
}

interface GuildMemberAddHandler : MemberEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.GUILD_MEMBER_ADD)
}

interface GuildMemberUpdateHandler : MemberEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.GUILD_MEMBER_UPDATE)
}

interface GuildMemberRemoveHandler : MemberEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.GUILD_MEMBER_REMOVE)
}

interface MessageCreateHandler : MessageEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.MESSAGE_CREATE)
}

interface AtMessageCreateHandler : MessageEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.AT_MESSAGE_CREATE)
}

interface DirectMessageCreateHandler : MessageEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.DIRECT_MESSAGE_CREATE)
}


interface MessageAuditPassHandler : MessageAuditEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.MESSAGE_AUDIT_PASS)
}

interface MessageAuditRejectHandler : MessageAuditEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.MESSAGE_AUDIT_REJECT)
}

interface MessageReactionAddHandler : MessageReactionEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.MESSAGE_REACTION_ADD)
}

interface MessageReactionRemoveHandler : MessageReactionEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.MESSAGE_REACTION_REMOVE)
}

interface AudioStartHandler : AudioEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.AUDIO_START)
}

interface AudioFinishHandler : AudioEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.AUDIO_FINISH)
}

interface AudioOnMicHandler : AudioEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.AUDIO_ON_MIC)
}

interface AudioOffMicHandler : AudioEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.AUDIO_OFF_MIC)
}

interface ForumThreadCreateHandler : ForumThreadEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.FORUM_THREAD_CREATE)
}

interface ForumThreadUpdateHandler : ForumThreadEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.FORUM_THREAD_UPDATE)
}

interface ForumThreadDeleteHandler : ForumThreadEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.FORUM_THREAD_DELETE)
}

interface ForumPostCreateHandler : ForumPostEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.FORUM_POST_CREATE)
}

interface ForumPostDeleteHandler : ForumPostEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.FORUM_POST_DELETE)
}

interface ForumReplyCreateHandler : ForumReplyEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.FORUM_REPLY_CREATE)
}

interface ForumReplyDeleteHandler : ForumReplyEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.FORUM_REPLY_DELETE)
}

interface ForumPublishAuditResultHandler : BotEventHandler<AuditResult> {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.FORUM_PUBLISH_AUDIT_RESULT)
}

interface OpenForumThreadCreateHandler : OpenForumEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.OPEN_FORUM_THREAD_CREATE)
}

interface OpenForumThreadUpdateHandler : OpenForumEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.OPEN_FORUM_THREAD_UPDATE)
}

interface OpenForumThreadDeleteHandler : OpenForumEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.OPEN_FORUM_THREAD_DELETE)
}

interface OpenForumPostCreateHandler : OpenForumEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.OPEN_FORUM_POST_CREATE)
}

interface OpenForumPostDeleteHandler : OpenForumEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.OPEN_FORUM_POST_DELETE)
}

interface OpenForumReplyCreateHandler : OpenForumEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.OPEN_FORUM_REPLY_CREATE)
}

interface OpenForumReplyDeleteHandler : OpenForumEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.OPEN_FORUM_REPLY_DELETE)
}

interface AudioOrLiveChannelMemberEnterHandler : LiveChannelEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.AUDIO_OR_LIVE_CHANNEL_MEMBER_ENTER)
}

interface AudioOrLiveChannelMemberExitHandler : LiveChannelEventHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.AUDIO_OR_LIVE_CHANNEL_MEMBER_EXIT)
}

interface DirectMessageDeleteHandler : MessageDeleteHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.DIRECT_MESSAGE_DELETE)
}

interface PublicMessageDeleteHandler : MessageDeleteHandler {
    override val acceptEvents: Array<Event>
        get() = arrayOf(Event.PUBLIC_MESSAGE_DELETE)
}
