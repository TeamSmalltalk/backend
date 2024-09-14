package smalltalk.backend.application.room

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionSubscribeEvent
import smalltalk.backend.application.message.MessageBroker
import smalltalk.backend.config.websocket.WebSocketConfig
import smalltalk.backend.domain.room.Room
import smalltalk.backend.exception.room.situation.DoesntExistRoomIdException
import smalltalk.backend.infra.repository.room.RoomRepository
import smalltalk.backend.presentation.dto.message.BotText
import smalltalk.backend.presentation.dto.message.Bot


@Component
class RoomEventListener(
    private val roomRepository: RoomRepository,
    private val broker: MessageBroker
) {
    private val logger = KotlinLogging.logger { }
    companion object {
        private const val DESTINATION_PATTERN = "^${WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX}[0-9]+$"
        private const val ROOM_ID_START_INDEX = 7
        private const val OPENED_ROOM = 1
        private const val ENTERED_ROOM_MIN = 2
        private const val ENTERED_ROOM_MAX = 10
        const val NICKNAME_PREFIX = "익명"
    }

    @EventListener
    private fun handleSubscribe(event: SessionSubscribeEvent) {
        val validatedDestination = validateDestination(StompHeaderAccessor.wrap(event.message).destination)
        val roomId = getRoomId(validatedDestination)
        sendBotMessage(
            validatedDestination,
            createEntranceMessageByCase(roomRepository.getById(roomId))
        )
    }

    private fun validateDestination(destination: String?) =
        destination?.takeIf {
            Regex(DESTINATION_PATTERN).matches(destination)
        } ?: throw DoesntExistRoomIdException()

    private fun getRoomId(destination: String) =
        destination.substring(ROOM_ID_START_INDEX).toLong()

    private fun sendBotMessage(topic: String, message: Any) {
        broker.send(topic, message)
    }

    private fun createEntranceMessageByCase(room: Room) =
        when (val numberOfMember = room.members.size) {
            OPENED_ROOM -> {
                Bot(
                    numberOfMember,
                    room.name + BotText.OPEN
                )
            }
            in ENTERED_ROOM_MIN..ENTERED_ROOM_MAX -> {
                Bot(
                    numberOfMember,
                    NICKNAME_PREFIX + room.members.last() + BotText.ENTRANCE
                )
            }
            else -> throw IllegalStateException("채팅방 인원수가 적절하지 않습니다")
        }
}