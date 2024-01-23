package smalltalk.backend.presentation.controller

import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import smalltalk.backend.presentation.dto.chatmessage.ChatMessage
import smalltalk.backend.ChatService

@Controller
class ChatRoomController (
    private val chattingService: ChatService
) {

    @MessageMapping("/{roomId}")
    fun send(@DestinationVariable roomId: String, chattingMessage: ChatMessage) {
        chattingService.send(roomId, chattingMessage)
    }
}