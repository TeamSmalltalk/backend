package smalltalk.backend.application.service.room

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import smalltalk.backend.util.message.MessageBroker
import smalltalk.backend.config.websocket.WebSocketConfig
import smalltalk.backend.presentation.dto.message.Chat


@Service
class RoomService(private val messageBroker: MessageBroker) {
    private val logger = KotlinLogging.logger { }

    fun send(id: String, message: Chat) {
        messageBroker.send(WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX + id, message)
    }
}