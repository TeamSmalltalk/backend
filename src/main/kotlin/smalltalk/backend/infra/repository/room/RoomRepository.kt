package smalltalk.backend.infra.repository.room

import smalltalk.backend.domain.room.Room

interface RoomRepository {
    fun save(roomName: String): Room
    fun getById(roomId: Long): Room
    fun findAll(): List<Room>
    fun deleteById(roomId: Long)
    fun deleteAll()
    fun addMember(room: Room): Long
    fun deleteMember(room: Room, memberId: Long)
}