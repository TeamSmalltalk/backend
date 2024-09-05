package smalltalk.backend.presentation.controller.room

import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import smalltalk.backend.application.room.RoomService
import smalltalk.backend.presentation.dto.message.Chat

@Controller
class RoomController (
    private val roomService: RoomService
) {

    @MessageMapping("{roomId}")
    fun send(@DestinationVariable("roomId") roomId: String, message: Chat) {
        roomService.send(roomId, message)
    }
}