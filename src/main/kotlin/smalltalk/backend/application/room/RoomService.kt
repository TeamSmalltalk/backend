package smalltalk.backend.application.room

import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.web.socket.messaging.SessionSubscribeEvent
import smalltalk.backend.application.message.MessageBroker
import smalltalk.backend.config.websocket.WebSocketConfig
import smalltalk.backend.logger
import smalltalk.backend.presentation.dto.message.Chat


@Service
class RoomService (
    private val messageBroker: MessageBroker
) {
    private val nicknamePrefix = "익명"

    fun send(roomId: String, message: Chat) {
        logger.debug { "call send method in service" }
        messageBroker.send(WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX + roomId, message)
    }

    @EventListener
    fun enter(event: SessionSubscribeEvent) {


    }

    fun open() {

    }

    fun exit() {
    }
}