package smalltalk.backend.infrastructure.repository.room

import org.springframework.stereotype.Repository
import smalltalk.backend.domain.room.Room

@Repository
class RedisRoomRepository : RoomRepository {
    override fun save(chatRoomName: String) {
        TODO("Not yet implemented")
    }

    override fun findById(chatRoomId: Long): Room {
        TODO("Not yet implemented")
    }

    override fun findAll(): Room {
        TODO("Not yet implemented")
    }

    override fun deleteById() {
        TODO("Not yet implemented")
    }

    override fun addMember(): Room {
        TODO("Not yet implemented")
    }

    override fun deleteMember(chatRoomMemberId: Long) {
        TODO("Not yet implemented")
    }
}