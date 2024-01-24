package smalltalk.backend.application.implement.message

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import smalltalk.backend.presentation.dto.chatmessage.ChatMessage

@Component
class SimpleMessageBroker(
    private val simpMessagingTemplate: SimpMessagingTemplate
) : MessageBroker {
    override fun send(chatRoomId: Long, chatMessage: ChatMessage) {
        TODO("Not yet implemented")
    }
}