package smalltalk.backend.application.service.room

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import org.springframework.web.socket.messaging.SessionSubscribeEvent
import smalltalk.backend.application.implement.room.RoomManager
import smalltalk.backend.application.implement.message.MessageBroker
import smalltalk.backend.config.websocket.WebSocketConfig
import smalltalk.backend.presentation.dto.message.Message


@Service
class RoomService (
    private val roomManager: RoomManager,
    private val messageBroker: MessageBroker
) {

    private val logger = KotlinLogging.logger {}
    private val nicknamePrefix = "익명"

    fun send(roomId: String, message: Message) {
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