package smalltalk.backend.application.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionSubscribeEvent
import smalltalk.backend.config.websocket.WebSocketConfig
import smalltalk.backend.domain.room.Room
import smalltalk.backend.presentation.exception.room.situation.DoesntExistRoomIdException
import smalltalk.backend.infra.repository.room.RoomRepository
import smalltalk.backend.presentation.dto.message.BotText
import smalltalk.backend.presentation.dto.message.Entrance


@Component
class WebSocketEventListener(
    private val roomRepository: RoomRepository,
    private val template: SimpMessagingTemplate,
) {
    private val logger = KotlinLogging.logger { }
    companion object {
        private const val DESTINATION_PATTERN = "^${WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX}[0-9]+$"
        private const val ROOM_ID_START_INDEX = 7
        private const val OPENED_ROOM = 1
        private const val ENTERED_ROOM_MIN = 2
        private const val ENTERED_ROOM_MAX = 10
        private const val NICKNAME_PREFIX = "익명"
    }

    /**
     * TODO 예외 발생 -> Error message (code 포함) 전송
     */
    @EventListener
    private fun handleSubscribe(event: SessionSubscribeEvent) {
        val destination = validateDestination(StompHeaderAccessor.wrap(event.message).destination)
        val roomId = getRoomId(destination)
        template.convertAndSend(
            destination,
            roomRepository.run {
                createEntranceMessageByCase(
                    addMember(roomId),
                    getById(roomId)
                )
            }
        )
    }

    private fun validateDestination(destination: String?) =
        destination?.takeIf {
            Regex(DESTINATION_PATTERN).matches(destination)
        } ?: throw DoesntExistRoomIdException()

    private fun getRoomId(destination: String) =
        destination.substring(ROOM_ID_START_INDEX).toLong()

    private fun createEntranceMessageByCase(enteredMemberId: Long, room: Room): Entrance {
        val numberOfMember = room.members.size
        val nickname = NICKNAME_PREFIX + enteredMemberId
        when (numberOfMember) {
            OPENED_ROOM -> {
                return Entrance(
                    numberOfMember,
                    nickname,
                    room.name + BotText.OPEN
                )
            }
            in ENTERED_ROOM_MIN..ENTERED_ROOM_MAX -> {
                return Entrance(
                    numberOfMember,
                    nickname,
                    nickname + BotText.ENTRANCE
                )
            }
            else -> throw IllegalStateException("채팅방 인원수가 적절하지 않습니다")
        }
    }
}