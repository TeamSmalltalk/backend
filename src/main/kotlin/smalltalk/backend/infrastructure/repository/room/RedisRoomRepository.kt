package smalltalk.backend.infrastructure.repository.room

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import smalltalk.backend.application.exception.room.situation.GeneratingRoomIdFailureException
import smalltalk.backend.domain.room.Room

@Repository
class RedisRoomRepository(
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper
) : RoomRepository {
    companion object {
        private const val ID_QUEUE_INITIAL_ID = 2L
        private const val ID_QUEUE_LIMIT_ID = 10L
        private const val MEMBERS_INITIAL_ID = 1L
        private const val ROOM_COUNTER_KEY = "roomCounter"
        private const val ROOM_KEY_PREFIX = "room:"
        private const val ROOM_KEY_PATTERN = "$ROOM_KEY_PREFIX*"
    }

    override fun save(roomName: String): Room {
        val generatedRoomId = generateRoomId()
        val room =
            Room(
                generatedRoomId,
                roomName,
                (ID_QUEUE_INITIAL_ID..ID_QUEUE_LIMIT_ID).toMutableList(),
                mutableListOf(MEMBERS_INITIAL_ID)
            )
        redisTemplate.opsForValue()[ROOM_KEY_PREFIX + generatedRoomId] = convertTypeToString(room)
        return room
    }

    override fun findById(roomId: Long): Room? = findByKey(ROOM_KEY_PREFIX + roomId)

    override fun findAll() =
        findKeysByPattern(ROOM_KEY_PATTERN).mapNotNull {
            findByKey(it)
        }

    override fun deleteById(roomId: Long) {
        redisTemplate.delete(ROOM_KEY_PREFIX + roomId)
    }

    override fun deleteMember(room: Room, memberId: Long) =
        room.apply {
            members.remove(memberId)
            idQueue.add(memberId)
        }

    override fun update(room: Room) {
        val key = (ROOM_KEY_PREFIX + room.id).toByteArray()
        redisTemplate.execute {
            return@execute it.apply {
                watch(key)
                multi()
                commands().set(
                    key,
                    convertTypeToString(
                        room.apply {
                            members.add(idQueue.removeFirst())
                        }
                    ).toByteArray()
                )
            }.exec()
        }
    }

    override fun deleteAll() {
        redisTemplate.run {
            delete(ROOM_COUNTER_KEY)
            delete(findKeysByPattern(ROOM_KEY_PATTERN))
        }
    }

    private fun generateRoomId() = redisTemplate.opsForValue().increment(ROOM_COUNTER_KEY) ?: throw GeneratingRoomIdFailureException()

    private fun findByKey(key: String) =
        redisTemplate.opsForValue()[key]?.let {
            objectMapper.readValue(it, Room::class.java)
        }

    private fun findKeysByPattern(key: String) = redisTemplate.keys(key)

    private fun convertTypeToString(room: Room) = objectMapper.writeValueAsString(room)
}