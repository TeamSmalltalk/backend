package smalltalk.backend.infra.repository.room

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import smalltalk.backend.infra.exception.room.situation.RoomIdNotGeneratedException
import smalltalk.backend.domain.room.Room
import smalltalk.backend.infra.exception.room.situation.RoomNotFoundException

@Repository
class RedisRoomRepository(
    private val template: StringRedisTemplate,
    private val mapper: ObjectMapper
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
        template.opsForValue()[ROOM_KEY_PREFIX + generatedRoomId] = convertToStringValue(room)
        return room
    }

    override fun getById(roomId: Long) =
        findByKey(ROOM_KEY_PREFIX + roomId) ?: throw RoomNotFoundException()

    override fun findAll() =
        findKeysByPattern().mapNotNull { findByKey(it) }

    override fun deleteById(roomId: Long) {
        TODO("분산락 적용")
    }

    override fun deleteAll() {
        template.run {
            delete(ROOM_COUNTER_KEY)
            delete(findKeysByPattern())
        }
    }

    override fun addMember(room: Room): Long {
        TODO("분산락 적용")
    }

    override fun deleteMember(room: Room, memberId: Long) {
        TODO("분산락 적용")
    }

    private fun generateRoomId() =
        template.opsForValue().increment(ROOM_COUNTER_KEY) ?: throw RoomIdNotGeneratedException()

    private fun convertToStringValue(value: Room) =
        mapper.writeValueAsString(value)

    private fun findByKey(key: String) =
        template.opsForValue()[key]?.let { mapper.readValue(it, Room::class.java) }

    private fun findKeysByPattern() =
        template.keys(ROOM_KEY_PATTERN)
}