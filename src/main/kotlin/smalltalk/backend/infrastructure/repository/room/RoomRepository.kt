package smalltalk.backend.infrastructure.repository.room

import smalltalk.backend.domain.room.Room

interface RoomRepository {
    fun save(chatRoomName: String)
    fun findById(chatRoomId: Long): Room
    fun findAll(): Room
    fun deleteById()
    fun addMember(): Room
    fun deleteMember(chatRoomMemberId: Long)
}