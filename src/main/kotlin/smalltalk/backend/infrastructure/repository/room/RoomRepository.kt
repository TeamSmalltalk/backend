package smalltalk.backend.infrastructure.repository.room

import smalltalk.backend.domain.room.Room

interface RoomRepository {
    fun save(roomName: String): Room
    fun findById(roomId: Long): Room?
    fun findAll(): List<Room>
    fun deleteMember(room: Room, memberId: Long): Room
    fun update(room: Room)
    fun deleteById(roomId: Long)
    fun deleteAll()
}