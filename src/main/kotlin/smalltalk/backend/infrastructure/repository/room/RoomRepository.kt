package smalltalk.backend.infrastructure.repository.room

import smalltalk.backend.domain.room.Room

interface RoomRepository {
    fun save(chatRoomName: String): Long?
    fun findById(chatRoomId: Long?): Room?
    fun findAll(): Set<Room>
    fun deleteById(chatRoomId: Long): Long
    fun addMember(chatRoomId: Long)
    fun deleteMember(chatRoomId: Long, memberId: Long)
}