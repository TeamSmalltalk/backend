package smalltalk.backend.infra.repository.room

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Repository
import smalltalk.backend.application.exception.room.situation.RoomIdNotGeneratedException
import smalltalk.backend.domain.room.Room

@Repository
class RedisRoomRepository(
    private val template: RedisTemplate<String, Any>
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
        template.opsForValue()[ROOM_KEY_PREFIX + generatedRoomId] = room
        return room
    }

    override fun findById(roomId: Long) =
        template.opsForValue()[ROOM_KEY_PREFIX + roomId] as? Room

    override fun findAll() =
        findKeysByPattern().mapNotNull { findById(it.substring(5).toLong()) }

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

    private fun findKeysByPattern() =
        template.keys(ROOM_KEY_PATTERN)
}