package smalltalk.backend.infrastructure.repository.room

import smalltalk.backend.domain.room.Room

interface RoomRepository {
    fun save(name: String): Room
    fun findById(id: Long): Room?
    fun getById(id: Long): Room
    fun findAll(): List<Room>
    fun deleteAll()
    fun addMember(id: Long): Long
    fun deleteMember(id: Long, memberId: Long): Room?
}