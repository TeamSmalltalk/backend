package smalltalk.backend.infrastructure.repository

import org.springframework.stereotype.Repository
import smalltalk.backend.domain.chatroom.ChatRoom

@Repository
class RedisChatRoomRepository : ChatRoomRepository {
    override fun save(chatRoomName: String) {
        TODO("Not yet implemented")
    }

    override fun findById(chatRoomId: Long): ChatRoom {
        TODO("Not yet implemented")
    }

    override fun findAll(): ChatRoom {
        TODO("Not yet implemented")
    }

    override fun deleteById() {
        TODO("Not yet implemented")
    }

    override fun addMember(): ChatRoom {
        TODO("Not yet implemented")
    }

    override fun deleteMember(chatRoomMemberId: Long) {
        TODO("Not yet implemented")
    }
}