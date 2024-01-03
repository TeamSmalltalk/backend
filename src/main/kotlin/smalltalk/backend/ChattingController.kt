package smalltalk.backend

import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
class ChattingController (
    private val chattingService: ChattingService
) {

    @MessageMapping("/{roomId}")
    fun send(@DestinationVariable roomId: String, chattingMessage: ChattingMessage) {
        chattingService.send(roomId, chattingMessage)
    }
}