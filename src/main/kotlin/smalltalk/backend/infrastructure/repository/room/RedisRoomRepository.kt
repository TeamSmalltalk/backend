package smalltalk.backend.infrastructure.repository.room

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import smalltalk.backend.domain.room.Room

@Repository
class RedisRoomRepository(
    private val redisTemplate: RedisTemplate<String, Room>
) : RoomRepository {
    companion object {
        private const val CHAT_ROOM_LIMIT_MEMBER_COUNT = 10

        private const val CHAT_ROOM_COUNTER = "roomCounter"
        private const val CHAT_ROOM_KEY = "room:"
        private const val CHAT_ROOM_NAME_KEY = ":name"
        private const val CHAT_ROOM_ID_QUEUE_KEY = ":idQueue"
        private const val CHAT_ROOM_MEMBERS_KEY = ":members"
    }

    override fun save(chatRoomName: String): Long? {

        val chatRoomId = generateChatRoomId()
        redisTemplate.opsForValue()["room:$chatRoomId"] = Room(chatRoomId, chatRoomName, (1..10).toMutableList(), mutableListOf())

        return chatRoomId
    }

    override fun findById(chatRoomId: Long?): Room? = TODO()

    override fun findAll(): Set<Room> {
        TODO("Not yet implemented")
//        redisTemplate.keys("room:*")
    }

    override fun deleteById(chatRoomId: Long): Long = TODO()

    override fun addMember(chatRoomId: Long) {
        TODO("Not yet implemented")
    }

    override fun deleteMember(chatRoomId: Long, memberId: Long) {
        TODO("Not yet implemented")
    }

    private fun generateChatRoomId(): Long? = redisTemplate.opsForValue().increment(CHAT_ROOM_COUNTER)
}