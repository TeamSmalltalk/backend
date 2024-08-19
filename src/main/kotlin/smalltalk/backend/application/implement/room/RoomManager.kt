package smalltalk.backend.application.implement.room

import org.springframework.stereotype.Component
import smalltalk.backend.application.exception.room.situation.FullRoomException
import smalltalk.backend.application.exception.room.situation.RoomNotFoundException
import smalltalk.backend.domain.room.Room
import smalltalk.backend.infra.repository.room.RoomRepository
import smalltalk.backend.presentation.dto.room.response.Open

@Component
class RoomManager(
    private val roomRepository: RoomRepository
) {
    fun create(roomName: String): Open {
        val savedRoom = roomRepository.save(roomName)
        return toOpen(savedRoom.id, savedRoom.members.last())
    }

    fun read(roomId: Long) =
        roomRepository.findById(roomId) ?: throw RoomNotFoundException()

    fun readAll() =
        roomRepository.findAll().map {
            it.run {
                toSimpleInfo(id, name, members.size)
            }
        }

    fun addMember(roomId: Long): Long {
        TODO("Room Repository 구현 후 작성")
    }

    private fun checkFull(roomToCheck: Room) {
        if (roomToCheck.members.size == 10) {
            throw FullRoomException()
        }
    }
}