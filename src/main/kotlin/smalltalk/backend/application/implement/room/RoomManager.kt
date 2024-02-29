package smalltalk.backend.application.implement.room

import org.springframework.stereotype.Component
import smalltalk.backend.application.exception.room.situation.RoomNotFoundException
import smalltalk.backend.infrastructure.repository.room.RoomRepository
import smalltalk.backend.presentation.dto.room.OpenResponse

@Component
class RoomManager(
    private val roomRepository: RoomRepository
) {
    fun create(roomName: String): OpenResponse {
        val savedRoom = roomRepository.save(roomName)
        return toOpenResponse(savedRoom.id, savedRoom.members.last())
    }

    fun read(roomId: Long) = roomRepository.findById(roomId)?: throw RoomNotFoundException()
}