package smalltalk.backend.application.room

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.event.EventListener
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionSubscribeEvent
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent
import smalltalk.backend.application.message.MessageBroker
import smalltalk.backend.application.room.Type.ENTER
import smalltalk.backend.application.room.Type.OPEN
import smalltalk.backend.config.websocket.WebSocketConfig
import smalltalk.backend.infra.repository.member.MemberRepository
import smalltalk.backend.infra.repository.room.RoomRepository
import smalltalk.backend.presentation.dto.message.Bot
import smalltalk.backend.presentation.dto.message.BotText

// TODO Implement layer 추가해서 로직 단순화
@Component
class RoomEventListener(
    @Qualifier("clientOutboundChannel")
    private val outboundChannel: MessageChannel,
    private val roomRepository: RoomRepository,
    private val memberRepository: MemberRepository,
    private val broker: MessageBroker,
) {
    private val logger = KotlinLogging.logger { }
    companion object {
        const val NICKNAME_PREFIX = "익명"
        const val TYPE_HEADER = "type"
        const val MEMBER_ID_HEADER = "memberId"
        private const val ROOM_ID_START_INDEX = 7
    }

    @EventListener
    private fun handleSubscribe(event: SessionSubscribeEvent) {
        logger.info { event.message }
        val sessionId = StompHeaderAccessor.wrap(event.message).sessionId ?: throw IllegalStateException("Doesnt exist session-id")
        val subscriptionId = StompHeaderAccessor.wrap(event.message).subscriptionId ?: throw IllegalStateException("Doesnt exist subscription-id")
        StompHeaderAccessor.wrap(event.message).run {
            try {
                val topic = destination?.takeIf {
                    Regex("^${WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX}[0-9]+$").matches(it)
                } ?: throw IllegalStateException("Doesnt exist room id")
                val roomId = topic.substring(ROOM_ID_START_INDEX).toLong()
                val memberId = getFirstNativeHeader(MEMBER_ID_HEADER)?.toLong()
                    ?: throw IllegalStateException("Doesnt exist member id")
                val room = roomRepository.getById(roomId)
                when (getFirstNativeHeader(TYPE_HEADER) ?: throw IllegalStateException("Doesnt exist type")) {
                    OPEN.name -> broker.send(
                        topic,
                        createBotMessage(room.members.size, room.name + BotText.OPEN)
                    )
                    ENTER.name ->
                        broker.send(
                            topic,
                            createBotMessage(room.members.size, NICKNAME_PREFIX + memberId + BotText.ENTRANCE)
                        )
                    else -> throw IllegalStateException("Not allowed type")
                }
                memberRepository.save(
                    sessionId,
                    memberId,
                    roomId
                )
            }
            catch (exceptionToHandle: Exception) {
                outboundChannel.send(
                    MessageBuilder.createMessage(
                        "600".toByteArray(),
                        StompHeaderAccessor.create(StompCommand.MESSAGE).apply {
                            this.sessionId = sessionId
                            this.subscriptionId = subscriptionId
                            addNativeHeader(TYPE_HEADER, "ERROR")
                        }.messageHeaders
                    )
                )
            }
        }
    }

    @EventListener
    private fun handleUnsubscribe(event: SessionUnsubscribeEvent) {
        logger.info { event.message }
        StompHeaderAccessor.wrap(event.message).let {
            val sessionId = it.sessionId ?: throw IllegalStateException("Not valid session-id")
            memberRepository.findById(sessionId)?.let { member ->
                val roomId = member.roomId
                val memberIdToDelete = member.id
                memberRepository.deleteById(sessionId)
                roomRepository.deleteMember(roomId, memberIdToDelete)
                roomRepository.findById(roomId)?.let { room ->
                    broker.send(
                        WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX + roomId,
                        createBotMessage(room.members.size, NICKNAME_PREFIX + memberIdToDelete + BotText.EXIT)
                    )
                }
            }
        }
    }

    private fun createBotMessage(numberOfMember: Int, message: String) = Bot(numberOfMember, message)
}