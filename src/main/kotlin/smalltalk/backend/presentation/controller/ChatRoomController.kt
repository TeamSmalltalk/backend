package smalltalk.backend.presentation.controller

import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import smalltalk.backend.application.service.chatroom.ChatRoomService
import smalltalk.backend.presentation.dto.chatmessage.ChatMessage

@Controller
class ChatRoomController (
    private val chatRoomService: ChatRoomService
) {

    @MessageMapping("/{roomId}")
    fun send(@DestinationVariable("roomId") roomId: String, chatMessage: ChatMessage) {
        chatRoomService.send(roomId, chatMessage)
    }
}