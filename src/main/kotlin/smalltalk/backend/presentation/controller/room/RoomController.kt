package smalltalk.backend.presentation.controller.room

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
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

    @PostMapping("/{id}")
    fun enter(@PathVariable("id") id: String) = ResponseEntity.ok(roomService.enter(id))
}