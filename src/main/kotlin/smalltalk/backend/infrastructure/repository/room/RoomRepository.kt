package smalltalk.backend.infrastructure.repository.room

import org.springframework.stereotype.Repository
import smalltalk.backend.domain.room.Room

interface RoomRepository {
    fun save(roomName: String): Long?
    fun findById(roomId: Long?): Room?
    fun findAll(): List<Room>
    fun deleteById(roomId: Long): Long
    fun addMember(roomId: Long)
    fun deleteMember(roomId: Long, memberId: Long)
    fun deleteAll(): Long
}