package smalltalk.backend.infra.repository.room

import smalltalk.backend.domain.room.Room


interface RoomRepository {
    fun save(roomName: String): Room
    fun findById(roomId: Long): Room?
    fun getById(roomId: Long): Room
    fun findAll(): List<Room>
    fun deleteAll()
    fun addMember(roomId: Long): Long
    fun deleteMember(roomId: Long, memberId: Long)
}