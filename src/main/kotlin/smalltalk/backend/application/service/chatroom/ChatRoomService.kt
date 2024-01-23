package smalltalk.backend.application.service.chatroom

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import smalltalk.backend.presentation.dto.chatmessage.ChatMessage


@Service
class ChatRoomService (
    private val simpMessagingTemplate: SimpMessagingTemplate
) {

    fun send(roomId: String, chattingMessage: ChatMessage) {
        simpMessagingTemplate.convertAndSend(roomId, chattingMessage)
    }
}