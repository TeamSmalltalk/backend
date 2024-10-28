package smalltalk.backend.infrastructure.repository.room

import io.github.oshai.kotlinlogging.KotlinLogging
import org.redisson.api.RedissonClient
import org.redisson.api.options.KeysScanParams
import org.springframework.stereotype.Repository
import smalltalk.backend.domain.room.Room
import smalltalk.backend.exception.room.situation.RoomNotFoundException

@Repository
class RedisLuaRoomRepository(private val client: RedissonClient) : RoomRepository {
    private val logger = KotlinLogging.logger { }
    companion object {
        private const val ROOM_COUNTER_KEY = "roomCounter"
        private const val ROOM_KEY_PREFIX = "room:"
        private const val ROOM_KEY_PATTERN = "$ROOM_KEY_PREFIX*"
        private const val ID_QUEUE_INITIAL_ID = 2L
        private const val ID_QUEUE_LIMIT_ID = 10L
        private const val MEMBERS_INITIAL_ID = 1L
    }

    override fun save(name: String): Room {
        val generatedId = generateId()
        val roomToSave = Room(
            generatedId,
            name,
            (ID_QUEUE_INITIAL_ID..ID_QUEUE_LIMIT_ID).toMutableList(),
            mutableListOf(MEMBERS_INITIAL_ID)
        )
        client.getBucket<Room>(ROOM_KEY_PREFIX + generatedId).set(roomToSave)
        return roomToSave
    }

    override fun findById(id: Long): Room? {
        return client.getBucket<Room>(ROOM_KEY_PREFIX + id).get()
    }

    override fun getById(id: Long) = client.getBucket<Room>(ROOM_KEY_PREFIX + id).get() ?: throw RoomNotFoundException()

    override fun findAll(): List<Room> =
        client.run {
            keys.getKeys(KeysScanParams().pattern(ROOM_KEY_PATTERN)).mapNotNull { client.getBucket<Room>(it).get() }.toList()
        }

    override fun deleteAll() {
        client.keys.run {
            delete(ROOM_COUNTER_KEY)
            deleteByPattern(ROOM_KEY_PATTERN)
        }
    }

    override fun addMember(id: Long): Long {
        TODO("Not yet implemented")
    }

    override fun deleteMember(id: Long, memberId: Long): Room? {
        TODO("Not yet implemented")
    }

    private fun generateId() = client.getAtomicLong(ROOM_COUNTER_KEY).incrementAndGet()
}