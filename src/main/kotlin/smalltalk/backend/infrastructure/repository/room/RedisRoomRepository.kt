package smalltalk.backend.infrastructure.repository.room

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import smalltalk.backend.domain.room.Room

@Repository
class RedisRoomRepository(
    private val redisTemplate: RedisTemplate<String, Room>
) : RoomRepository {
    companion object {
        private const val ROOM_LIMIT_MEMBER_COUNT = 10

        private const val ROOM_COUNTER_KEY = "roomCounter"
        private const val ROOM_KEY = "room:"
    }

    override fun save(chatRoomName: String): Long? {

        val chatRoomId = generateChatRoomId()
        redisTemplate.opsForValue()[ROOM_KEY + chatRoomId] = Room(chatRoomId, chatRoomName, (1..ROOM_LIMIT_MEMBER_COUNT).toMutableList(), mutableListOf())

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

    private fun generateChatRoomId(): Long? = redisTemplate.opsForValue().increment(ROOM_COUNTER_KEY)
}