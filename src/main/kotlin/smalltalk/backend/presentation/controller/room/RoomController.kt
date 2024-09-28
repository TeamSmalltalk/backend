package smalltalk.backend.presentation.controller.room

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Controller
import smalltalk.backend.application.service.room.RoomService


@Controller
class RoomController(private val roomService: RoomService) {
    private val logger = KotlinLogging.logger { }


}