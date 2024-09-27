package ng.i.sav.qdroid.infra.client

import ng.i.sav.qdroid.infra.config.ComponentConfiguration
import ng.i.sav.qdroid.infra.model.Payload
import ng.i.sav.qdroid.log.Slf4kt
import org.springframework.stereotype.Component

@Component
class BotEventDispatcher(
    private val handlers: List<BotEventHandler<*>>,
    components: ComponentConfiguration
) {
    @Suppress("UNUSED")
    private val objectMapper = components.getObjectMapper()

    fun onEvent(bot: ApiRequest, payload: Payload<*>, event: String) {
        log.debug("Dispatcher msg: {}", event)

        val e = payload.t!!
        handlers.forEach { handler ->
            if (handler.acceptEvents.contains(e)) {
                handler.onEvent(
                    bot,
                    payload,
                    /*objectMapper.readValue(event, handler.javaClass.declaredMethods.let { methods ->
                        methods.first { it.name == "onEvent" && it.parameterCount == 3 && it.parameterTypes[1] != Any::class.java }
                            .let {
                                object : SimpleType(
                                    Payload::class.java,
                                    TypeBindings.create(
                                        Payload::class.java,
                                        constructUnsafe(it.parameterTypes[1])
                                    ),
                                    null, null,
                                ) {}
                            }
                    }),*/
                )
            }
        }
        /**
         * to [ng.i.sav.qdroid.infra.client.Event] to maintain the event type
         * */
        /*  when (Event.valueOf(payload.t!!)) {
              Event.GUILD_CREATE -> {

                  handlers.forEach { handler ->
                      if (handler is GuildCreateHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.GUILD_UPDATE -> {
                  handlers.forEach { handler ->
                      if (handler is GuildUpdateHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.GUILD_DELETE -> {
                  handlers.forEach { handler ->
                      if (handler is GuildDeleteHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.CHANNEL_CREATE -> {
                  handlers.forEach { handler ->
                      if (handler is ChannelCreateHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.CHANNEL_UPDATE -> {
                  handlers.forEach { handler ->
                      if (handler is ChannelUpdateHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.CHANNEL_DELETE -> {
                  handlers.forEach { handler ->
                      if (handler is ChannelDeleteHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.GUILD_MEMBER_ADD -> {
                  handlers.forEach { handler ->
                      if (handler is GuildMemberAddHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.GUILD_MEMBER_UPDATE -> {
                  handlers.forEach { handler ->
                      if (handler is GuildMemberUpdateHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.GUILD_MEMBER_REMOVE -> {
                  handlers.forEach { handler ->
                      if (handler is GuildMemberRemoveHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.MESSAGE_CREATE -> {
                  handlers.forEach { handler ->
                      if (handler is AtMessageCreateHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
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
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.MESSAGE_REACTION_REMOVE -> {
                  handlers.forEach { handler ->
                      if (handler is MessageReactionRemoveHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.DIRECT_MESSAGE_CREATE -> {
                  handlers.forEach { handler ->
                      if (handler is DirectMessageCreateHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
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
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.OPEN_FORUM_THREAD_UPDATE -> {
                  handlers.forEach { handler ->
                      if (handler is OpenForumThreadUpdateHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.OPEN_FORUM_THREAD_DELETE -> {
                  handlers.forEach { handler ->
                      if (handler is OpenForumThreadDeleteHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.OPEN_FORUM_POST_CREATE -> {
                  handlers.forEach { handler ->
                      if (handler is OpenForumPostCreateHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.OPEN_FORUM_POST_DELETE -> {
                  handlers.forEach { handler ->
                      if (handler is OpenForumPostDeleteHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.OPEN_FORUM_REPLY_CREATE -> {
                  handlers.forEach { handler ->
                      if (handler is OpenForumReplyCreateHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.OPEN_FORUM_REPLY_DELETE -> {
                  handlers.forEach { handler ->
                      if (handler is OpenForumReplyDeleteHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.AUDIO_OR_LIVE_CHANNEL_MEMBER_ENTER -> {
                  handlers.forEach { handler ->
                      if (handler is AudioOrLiveChannelMemberEnterHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.AUDIO_OR_LIVE_CHANNEL_MEMBER_EXIT -> {
                  handlers.forEach { handler ->
                      if (handler is AudioOrLiveChannelMemberExitHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.INTERACTION_CREATE -> {}


              Event.MESSAGE_AUDIT_PASS -> {
                  handlers.forEach { handler ->
                      if (handler is MessageAuditPassHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.MESSAGE_AUDIT_REJECT -> {
                  handlers.forEach { handler ->
                      if (handler is MessageAuditRejectHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.FORUM_THREAD_CREATE -> {
                  handlers.forEach { handler ->
                      if (handler is ForumThreadCreateHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.FORUM_THREAD_UPDATE -> {
                  handlers.forEach { handler ->
                      if (handler is ForumThreadUpdateHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.FORUM_THREAD_DELETE -> {
                  handlers.forEach { handler ->
                      if (handler is ForumThreadDeleteHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.FORUM_POST_CREATE -> {
                  handlers.forEach { handler ->
                      if (handler is ForumPostCreateHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.FORUM_POST_DELETE -> {
                  handlers.forEach { handler ->
                      if (handler is ForumPostDeleteHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.FORUM_REPLY_CREATE -> {
                  handlers.forEach { handler ->
                      if (handler is ForumReplyCreateHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.FORUM_REPLY_DELETE -> {
                  handlers.forEach { handler ->
                      if (handler is ForumReplyDeleteHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.FORUM_PUBLISH_AUDIT_RESULT -> {
                  handlers.forEach { handler ->
                      if (handler is ForumPublishAuditResultHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.AUDIO_START -> {
                  handlers.forEach { handler ->
                      if (handler is AudioStartHandler) handler.onEvent(bot, objectMapper.toObj(payload))
                  }
              }

              Event.AUDIO_FINISH -> {
                  handlers.forEach { handler ->
                      if (handler is AudioFinishHandler) handler.onEvent(bot, objectMapper.toObj(payload))
                  }
              }

              Event.AUDIO_ON_MIC -> {
                  handlers.forEach { handler ->
                      if (handler is AudioOnMicHandler) handler.onEvent(bot, objectMapper.toObj(payload))
                  }
              }

              Event.AUDIO_OFF_MIC -> {
                  handlers.forEach { handler ->
                      if (handler is AudioOffMicHandler) handler.onEvent(bot, objectMapper.toObj(payload))
                  }
              }

              Event.AT_MESSAGE_CREATE -> {
                  handlers.forEach { handler ->
                      if (handler is AtMessageCreateHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }

              Event.PUBLIC_MESSAGE_DELETE -> {
                  handlers.forEach { handler ->
                      if (handler is MessageDeleteHandler) handler.onEvent(
                          bot,
                          objectMapper.toObj(payload)
                      )
                  }
              }
          }*/

    }

    companion object {
        private val log = Slf4kt.getLogger(BotEventDispatcher::class.java)
    }
}

