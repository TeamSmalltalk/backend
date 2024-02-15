package smalltalk.backend.infrastructure.repository.room

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.RedisTemplate
import smalltalk.backend.domain.room.Room

internal class RedisRoomRepository(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val objectMapper: ObjectMapper
) : RoomRepository {
    companion object {
        private const val ROOM_LIMIT_MEMBER_COUNT = 10
        private const val ROOM_COUNTER_KEY = "roomCounter"
        private const val ROOM_KEY = "room:"
    }

    override fun save(roomName: String): Long? {

        val roomId = generateChatRoomId()
        redisTemplate.opsForValue()[ROOM_KEY + roomId] =
            objectMapper.writeValueAsString(
                Room(
                    roomId,
                    roomName,
                    (1..ROOM_LIMIT_MEMBER_COUNT).toMutableList(), mutableListOf()
                )
            )

        return roomId
    }

    override fun findById(roomId: Long?): Room? =
        objectMapper.readValue(redisTemplate.opsForValue()[ROOM_KEY + roomId].toString(), Room::class.java)

    override fun findAll(): List<Room> {
        TODO("Not yet implemented")
    }

    override fun deleteById(roomId: Long): Long = TODO()

    override fun addMember(roomId: Long) {
        TODO("Not yet implemented")
    }

    override fun deleteMember(roomId: Long, memberId: Long) {
        TODO("Not yet implemented")
    }

    override fun deleteAll() = redisTemplate.delete(findKeysByPattern())

    private fun generateChatRoomId() = redisTemplate.opsForValue().increment(ROOM_COUNTER_KEY)

    private fun findKeysByPattern() = redisTemplate.keys("$ROOM_KEY*")
}