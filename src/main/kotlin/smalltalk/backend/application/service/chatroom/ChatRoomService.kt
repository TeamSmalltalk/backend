package smalltalk.backend.application.service.chatroom

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import smalltalk.backend.application.implement.chatroom.ChatRoomManager
import smalltalk.backend.application.implement.chatroom.ChatRoomResponseMapper
import smalltalk.backend.application.implement.message.MessageBroker
import smalltalk.backend.application.implement.message.SimpleMessageBroker
import smalltalk.backend.presentation.dto.chatmessage.ChatMessage


@Service
class ChatRoomService (
    private val chatRoomManager: ChatRoomManager,
    private val chatRoomResponseMapper: ChatRoomResponseMapper,
    private val messageBroker: MessageBroker
) {

    private val logger = KotlinLogging.logger {}
    private final val destinationPrefix = "/rooms/"
    private final val nicknamePrefix = "익명"

    fun send(roomId: String, chatMessage: ChatMessage) {
        logger.debug { "call send method in service" }
        messageBroker.send(destinationPrefix + roomId, chatMessage)
    }

    fun enter() {

    }

    fun open() {

    }

    fun exit() {

    }
}