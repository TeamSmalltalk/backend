package smalltalk.backend

import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class ChatRoomController (
    private val chattingService: ChatService
) {

    @MessageMapping("/{roomId}")
    fun send(@DestinationVariable roomId: String, chattingMessage: ChatMessage) {
        chattingService.send(roomId, chattingMessage)
    }
}