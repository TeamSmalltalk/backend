package smalltalk.backend.application.room

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionSubscribeEvent
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent
import smalltalk.backend.application.message.MessageBroker
import smalltalk.backend.domain.room.Room
import smalltalk.backend.infra.repository.room.RoomRepository
import smalltalk.backend.presentation.dto.message.Bot
import smalltalk.backend.presentation.dto.message.BotText

// TODO Custom layer 추가해서 로직 단순화
@Component
class RoomEventListener(
    private val roomRepository: RoomRepository,
    private val broker: MessageBroker
) {
    private val logger = KotlinLogging.logger { }
    companion object {
        const val NICKNAME_PREFIX = "익명"
        const val MEMBER_ID_HEADER = "memberId"
        private const val ROOM_ID_START_INDEX = 7
        private const val OPENED_ROOM = 1
    }

    // TODO 예외 처리 고민
    @EventListener
    private fun handleSubscribe(event: SessionSubscribeEvent) {
        val destination = getDestination(StompHeaderAccessor.wrap(event.message))
        sendBotMessage(destination, createEntranceMessageByCase(roomRepository.getById(getRoomId(destination))))
    }

    @EventListener
    private fun handleUnsubscribe(event: SessionUnsubscribeEvent) =
        StompHeaderAccessor.wrap(event.message).let {
            val memberIdToDelete = getMemberId(it)
            val destination = getDestination(it)
            val roomId = getRoomId(destination)
            roomRepository.deleteMember(roomId, memberIdToDelete)
            roomRepository.findById(roomId)?.let { room ->
                sendBotMessage(destination, createExitMessage(memberIdToDelete, room.members.size))
            } ?: return
        }

    private fun getDestination(accessor: StompHeaderAccessor) = accessor.destination ?: throw IllegalStateException("Doesnt exist destination")

    private fun getMemberId(accessor: StompHeaderAccessor) =
        accessor.getFirstNativeHeader(MEMBER_ID_HEADER)?.run { toLong() } ?: throw IllegalStateException("Doesnt exist member id")

    private fun getRoomId(destination: String) = destination.substring(ROOM_ID_START_INDEX).toLong()

    private fun sendBotMessage(topic: String, message: Bot) = broker.send(topic, message)

    private fun createEntranceMessageByCase(room: Room) =
        when (val numberOfMember = room.members.size) {
            OPENED_ROOM -> createBotMessage(numberOfMember, room.name + BotText.OPEN)
            else -> createBotMessage(numberOfMember, getNickname(room.members.last()) + BotText.ENTRANCE)
        }

    private fun createExitMessage(exitedMemberId: Long, numberOfMember: Int) =
        createBotMessage(numberOfMember, getNickname(exitedMemberId) + BotText.EXIT)

    private fun createBotMessage(numberOfMember: Int, message: String) =
        Bot(
            numberOfMember,
            message
        )

    private fun getNickname(memberId: Long) = NICKNAME_PREFIX + memberId
}