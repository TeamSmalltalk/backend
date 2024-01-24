package smalltalk.backend.infrastructure.repository

import smalltalk.backend.domain.chatroom.ChatRoom

interface ChatRoomRepository {
    fun save(chatRoomName: String)
    fun findById(chatRoomId: Long): ChatRoom
    fun findAll(): ChatRoom
    fun deleteById()
    fun addMember(): ChatRoom
    fun deleteMember(chatRoomMemberId: Long)
}