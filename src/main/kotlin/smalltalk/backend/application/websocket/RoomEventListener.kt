package smalltalk.backend.application.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionSubscribeEvent
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent
import smalltalk.backend.application.implement.room.RoomImplement
import smalltalk.backend.application.websocket.MessageHeader.*
import smalltalk.backend.application.websocket.SystemType.*
import smalltalk.backend.config.websocket.WebSocketConfig
import smalltalk.backend.exception.room.situation.DOESNT_EXIST_HEADER_MESSAGE_PREFIX
import smalltalk.backend.exception.room.situation.DoesntExistHeaderException

@Component
class RoomEventListener(private val roomImplement: RoomImplement) {
    private val logger = KotlinLogging.logger { }
    companion object {
        private const val DESTINATION_PATTERN = "^${WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX}[0-9]+$"
        private const val ROOM_ID_START_INDEX = 7
    }

    @EventListener
    private fun handleSubscribe(event: SessionSubscribeEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)
        val sessionId = getValue(accessor.sessionId, SESSION.key)
        val subscriptionId = getValue(accessor.subscriptionId, SUBSCRIPTION.key)
        try {
            val destination = getDestination(accessor.destination, ROOM.key)
            val id = getRoomId(destination).toLong()
            val memberId = getNativeHeaderValue(accessor, MEMBER.key).toLong()
            val room = roomImplement.getById(id)
            when (getNativeHeaderValue(accessor, TYPE.key)) {
                OPEN.name -> roomImplement.sendSystemMessage(destination, OPEN, room.members.size, room.name, memberId)
                ENTER.name -> roomImplement.sendSystemMessage(destination, ENTER, room.members.size, room.name, memberId)
                else -> throw DoesntExistHeaderException(TYPE.key)
            }
            roomImplement.saveMember(sessionId, memberId, id)
        }
        catch (exceptionToHandle: Exception) {
            var message = exceptionToHandle.message
            message?.let {
                if (checkExceptionMessagePrefix(it))
                    message = getExceptionCause(it)
            }
            roomImplement.sendErrorMessage(message, sessionId, subscriptionId)
        }
    }

    @EventListener
    private fun handleUnsubscribe(event: SessionUnsubscribeEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)
        val sessionId = getValue(accessor.sessionId, SESSION.key)
        roomImplement.findMemberById(sessionId)?.let { member ->
            val id = member.roomId
            val memberIdToDelete = member.id
            roomImplement.deleteMember(sessionId, id, memberIdToDelete)?.let { room ->
                roomImplement.sendSystemMessage(getDestination(id), EXIT, room.members.size, room.name, memberIdToDelete)
            }
        }
    }

    private fun getValue(valueToCheck: String?, nameIfCauseException: String) =
        valueToCheck ?: throw DoesntExistHeaderException(nameIfCauseException)

    private fun getDestination(destination: String?, nameIfCauseException: String) =
        destination?.takeIf { Regex(DESTINATION_PATTERN).matches(it) } ?: throw DoesntExistHeaderException(nameIfCauseException)

    private fun getDestination(id: Long) = WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX + id

    private fun getRoomId(destination: String) = destination.substring(ROOM_ID_START_INDEX)

    private fun getNativeHeaderValue(accessor: StompHeaderAccessor, key: String) =
        accessor.getFirstNativeHeader(key) ?: throw DoesntExistHeaderException(key)

    private fun checkExceptionMessagePrefix(message: String) = message.startsWith(DOESNT_EXIST_HEADER_MESSAGE_PREFIX)

    private fun getExceptionCause(message: String) = message.substringAfter(DOESNT_EXIST_HEADER_MESSAGE_PREFIX)
}