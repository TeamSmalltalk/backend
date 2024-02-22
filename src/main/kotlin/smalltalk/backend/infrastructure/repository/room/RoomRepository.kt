package smalltalk.backend.infrastructure.repository.room

import smalltalk.backend.domain.room.Room

interface RoomRepository {
    fun save(roomName: String): Room
    fun findById(roomId: Long): Room?
    fun findAll(): List<Room>
    fun addMember(room: Room): Room
    fun deleteMember(room: Room, memberId: Long): Room
    fun update(updatedRoom: Room)
    fun deleteById(roomId: Long)
    fun deleteAll()
}