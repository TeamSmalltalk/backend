package smalltalk.backend.domain.room

import smalltalk.backend.exception.room.situation.RoomNotFoundException

fun RoomRepository.getById(id: Long) = findById(id) ?: throw RoomNotFoundException()

interface RoomRepository {
    fun save(name: String): Room
    fun findById(id: Long): Room?
    fun findAll(): List<Room>
    fun deleteAll()
    fun addMember(id: Long): Long
    fun deleteMember(id: Long, memberId: Long): Room?
}