package smalltalk.backend.infrastructure.repository.room

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import smalltalk.backend.domain.room.Room
import java.math.BigInteger

@Repository
class RedisRoomRepository(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val objectMapper: ObjectMapper
) : RoomRepository {
    companion object {
        private const val ROOM_LIMIT_MEMBER_COUNT = 10
        private const val ROOM_COUNTER_KEY = "roomCounter"
        private const val ROOM_KEY = "room:"
    }

    override fun save(roomName: String): BigInteger? {
        val generatedRoomId = generateRoomId()?.toBigInteger()
        redisTemplate.opsForValue()[ROOM_KEY + generatedRoomId] =
            objectMapper.writeValueAsString(
                Room(
                    generatedRoomId,
                    roomName,
                    (1..ROOM_LIMIT_MEMBER_COUNT).toMutableList(),
                    mutableListOf()
                )
            )
        return generatedRoomId
    }

    override fun findById(roomId: BigInteger): Room? = findByKey(ROOM_KEY + roomId, Room::class.java)

    override fun findAll() =
        findKeysByPattern("$ROOM_KEY*").mapNotNull {
            findByKey(it, Room::class.java)
        }

    override fun deleteById(roomId: BigInteger) = redisTemplate.delete(ROOM_KEY + roomId)

    override fun addMember(room: Room) =
        room.apply {
            idQueue?.let {
                it.removeFirstOrNull()?.let { memberId ->
                    members?.add(memberId)
                }
            }
        }

    override fun deleteMember(roomId: BigInteger, memberId: BigInteger) {
        TODO("Not yet implemented")
    }

    override fun updateRoom(room: Room) {
        TODO("Not yet implemented")
    }

    override fun deleteAll() =
        redisTemplate.run {
            delete(ROOM_COUNTER_KEY)
            delete(findKeysByPattern("$ROOM_KEY*"))
        }

    private fun generateRoomId() = redisTemplate.opsForValue().increment(ROOM_COUNTER_KEY)

    private fun <T> findByKey(key: String, clazz: Class<T>) =
        redisTemplate.opsForValue()[key]?.let {
            objectMapper.readValue(it.toString(), clazz)
        }

    private fun findKeysByPattern(key: String) = redisTemplate.keys(key)
}