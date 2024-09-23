package ng.i.sav.qdroid.infra.client

import ng.i.sav.qdroid.infra.model.*
import ng.i.sav.qdroid.infra.model.event.*


interface BotEventHandler<T> {
    fun onEvent(bot: GuildBot, event: T, type: String)
}

interface GuildEventHandler : BotEventHandler<GuildEvent>
interface ChannelEventHandler : BotEventHandler<ChannelEvent>
interface MemberEventHandler : BotEventHandler<GuildMemberEvent>
interface MessageEventHandler : BotEventHandler<Message>
interface MessageAuditEventHandler : BotEventHandler<MessageAudited>
interface MessageReactionEventHandler : BotEventHandler<MessageReaction>
interface AudioEventHandler : BotEventHandler<AudioAction>
interface ForumThreadEventHandler : BotEventHandler<Thread>
interface ForumPostEventHandler : BotEventHandler<Post>
interface ForumReplyEventHandler : BotEventHandler<Reply>
interface OpenForumEventHandler : BotEventHandler<ng.i.sav.qdroid.infra.model.event.ForumEvent>
interface LiveChannelEventHandler : BotEventHandler<ng.i.sav.qdroid.infra.model.event.LiveChannelEvent>


interface GuildCreateHandler : GuildEventHandler
interface GuildUpdateHandler : GuildEventHandler
interface GuildDeleteHandler : GuildEventHandler
interface ChannelCreateHandler : ChannelEventHandler
interface ChannelUpdateHandler : ChannelEventHandler
interface ChannelDeleteHandler : ChannelEventHandler
interface GuildMemberAddHandler : MemberEventHandler
interface GuildMemberUpdateHandler : MemberEventHandler
interface GuildMemberRemoveHandler : MemberEventHandler
interface AtMessageCreateHandler : MessageEventHandler

//interface MessageDeleteHandler :
interface MessageAuditPassHandler : MessageAuditEventHandler
interface MessageAuditRejectHandler : MessageAuditEventHandler
interface DirectMessageCreateHandler : MessageEventHandler

//interface DirectMessageDeleteHandler :
interface MessageReactionAddHandler : MessageReactionEventHandler
interface MessageReactionRemoveHandler : MessageReactionEventHandler
interface AudioStartHandler : AudioEventHandler
interface AudioFinishHandler : AudioEventHandler
interface AudioOnMicHandler : AudioEventHandler
interface AudioOffMicHandler : AudioEventHandler
interface ForumThreadCreateHandler : ForumThreadEventHandler
interface ForumThreadUpdateHandler : ForumThreadEventHandler
interface ForumThreadDeleteHandler : ForumThreadEventHandler
interface ForumPostCreateHandler : ForumPostEventHandler
interface ForumPostDeleteHandler : ForumPostEventHandler
interface ForumReplyCreateHandler : ForumReplyEventHandler
interface ForumReplyDeleteHandler : ForumReplyEventHandler
interface ForumPublishAuditResultHandler : BotEventHandler<AuditResult>
interface OpenForumThreadCreateHandler : OpenForumEventHandler
interface OpenForumThreadUpdateHandler : OpenForumEventHandler
interface OpenForumThreadDeleteHandler : OpenForumEventHandler
interface OpenForumPostCreateHandler : OpenForumEventHandler
interface OpenForumPostDeleteHandler : OpenForumEventHandler
interface OpenForumReplyCreateHandler : OpenForumEventHandler
interface OpenForumReplyDeleteHandler : OpenForumEventHandler
interface AudioOrLiveChannelMemberEnterHandler : LiveChannelEventHandler
interface AudioOrLiveChannelMemberExitHandler : LiveChannelEventHandler
