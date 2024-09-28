package smalltalk.backend.presentation.controller.room

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import smalltalk.backend.application.service.room.RoomService
import smalltalk.backend.presentation.dto.room.request.OpenRequest


@RestController
@RequestMapping("/api/rooms")
class RoomController(private val roomService: RoomService) {
    private val logger = KotlinLogging.logger { }

    @PostMapping
    fun open(@RequestBody request: OpenRequest) = ResponseEntity.status(CREATED).body(roomService.open(request))

    @GetMapping
    fun getSimpleInfos() = ResponseEntity.ok(roomService.getSimpleInfos())
}