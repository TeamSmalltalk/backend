package smalltalk.backend.infrastructure.repository.room

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import smalltalk.backend.application.exception.room.situation.RoomIdNotFoundException
import smalltalk.backend.domain.room.Room

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

    override fun save(roomName: String): Long {
        val generatedRoomId = generateRoomId()
        redisTemplate.opsForValue()[ROOM_KEY + generatedRoomId] =
            convertTypeToString(
                Room(
                    generatedRoomId,
                    roomName,
                    (1..ROOM_LIMIT_MEMBER_COUNT).toMutableList(),
                    mutableListOf()
                )
            )
        return generatedRoomId
    }

    override fun findById(roomId: Long): Room? = findByKey(ROOM_KEY + roomId, Room::class.java)

    override fun findAll() =
        findKeysByPattern("$ROOM_KEY*").mapNotNull {
            findByKey(it, Room::class.java)
        }

    override fun deleteById(roomId: Long) {
        redisTemplate.delete(ROOM_KEY + roomId)
    }

    override fun addMember(room: Room) =
        room.apply {
            members.add(idQueue.removeFirst())
        }

    override fun deleteMember(room: Room, memberId: Int) =
        room.apply {
            members.remove(memberId)
            idQueue.add(memberId)
        }

    override fun update(updatedRoom: Room) {
        redisTemplate.opsForValue()[ROOM_KEY + updatedRoom.id] = convertTypeToString(updatedRoom)
    }

    override fun deleteAll() {
        redisTemplate.run {
            delete(ROOM_COUNTER_KEY)
            delete(findKeysByPattern("$ROOM_KEY*"))
        }
    }

    private fun generateRoomId() =
        redisTemplate.opsForValue().increment(ROOM_COUNTER_KEY)?: throw RoomIdNotFoundException()

    private fun <T> findByKey(key: String, clazz: Class<T>) =
        redisTemplate.opsForValue()[key]?.let {
            objectMapper.readValue(it.toString(), clazz)
        }

    private fun findKeysByPattern(key: String) = redisTemplate.keys(key)

    private fun convertTypeToString(any: Any) = objectMapper.writeValueAsString(any)
}