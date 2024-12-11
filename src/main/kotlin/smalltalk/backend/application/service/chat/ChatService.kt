package smalltalk.backend.application.service.chat

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import smalltalk.backend.config.websocket.WebSocketConfig
import smalltalk.backend.presentation.dto.message.Chat
import smalltalk.backend.infrastructure.broker.MessageBroker

@Service
class ChatService(private val broker: MessageBroker) {
    private val logger = KotlinLogging.logger { }

    fun send(id: String, message: Chat) {
        broker.send(WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX + id, message)
    }
}