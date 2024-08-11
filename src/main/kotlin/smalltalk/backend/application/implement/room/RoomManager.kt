package smalltalk.backend.application.implement.room

import org.springframework.stereotype.Component
import smalltalk.backend.application.exception.room.situation.FullRoomException
import smalltalk.backend.application.exception.room.situation.RoomNotFoundException
import smalltalk.backend.domain.room.Room
import smalltalk.backend.infrastructure.repository.room.RoomRepository
import smalltalk.backend.presentation.dto.room.OpenRoomResponse

@Component
class RoomManager(
    private val roomRepository: RoomRepository
) {
    fun create(roomName: String): OpenRoomResponse {
        val savedRoom = roomRepository.save(roomName)
        return toOpenRoomResponse(savedRoom.id, savedRoom.members.last())
    }

    fun read(roomId: Long) = roomRepository.findById(roomId) ?: throw RoomNotFoundException()

    fun readAll() =
        roomRepository.findAll().map {
            it.run {
                toSimpleRoomInfoResponse(id, name, members.size)
            }
        }

    fun addMember(roomId: Long): Long {
        var memberId: Long
        do {
            val readRoom = read(roomId)
            checkFull(readRoom)
            memberId = roomRepository.addMember(readRoom)
        } while (memberId == 0L)
        return memberId
    }

    private fun checkFull(roomToCheck: Room) {
        if (roomToCheck.members.size == 10) {
            throw FullRoomException()
        }
    }
}