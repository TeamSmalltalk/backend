package smalltalk.backend.application.implement.room

import org.springframework.stereotype.Component
import smalltalk.backend.application.exception.room.situation.RoomNotFoundException
import smalltalk.backend.infrastructure.repository.room.RoomRepository

@Component
class RoomManager(
    private val roomRepository: RoomRepository
) {
    fun read(roomId: Long) = roomRepository.findById(roomId)?: throw RoomNotFoundException()
}