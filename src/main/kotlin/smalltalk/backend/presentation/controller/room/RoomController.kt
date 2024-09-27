package smalltalk.backend.presentation.controller.room

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import smalltalk.backend.application.service.room.RoomService
import smalltalk.backend.presentation.dto.message.Chat


@Controller
class RoomController(private val roomService: RoomService) {
    private val logger = KotlinLogging.logger { }

    @MessageMapping("{id}")
    fun send(@DestinationVariable("id") id: String, message: Chat) {
        roomService.send(id, message)
    }
}