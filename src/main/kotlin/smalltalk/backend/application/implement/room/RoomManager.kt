package smalltalk.backend.application.implement.room

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import smalltalk.backend.application.exception.room.situation.RoomNotFoundException
import smalltalk.backend.infrastructure.repository.room.RoomRepository

@Component
class RoomManager(
    private val roomRepository: RoomRepository
) {
    fun read(roomId: Long) = roomRepository.findById(roomId) ?: throw RoomNotFoundException()
}