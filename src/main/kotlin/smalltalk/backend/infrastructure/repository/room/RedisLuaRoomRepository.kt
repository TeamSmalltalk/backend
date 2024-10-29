package smalltalk.backend.infrastructure.repository.room

import io.github.oshai.kotlinlogging.KotlinLogging
import org.redisson.api.RScript
import org.redisson.api.RedissonClient
import org.redisson.api.options.KeysScanParams
import org.redisson.client.codec.LongCodec
import org.redisson.client.codec.StringCodec
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import smalltalk.backend.domain.room.Room
import smalltalk.backend.exception.room.situation.RoomNotFoundException
import smalltalk.backend.util.jackson.ObjectMapperClient

//@Repository
class RedisLuaRoomRepository(
    private val redisson: RedissonClient,
    private val redisTemplate: StringRedisTemplate,
    private val mapper: ObjectMapperClient
) : RoomRepository {
    companion object {
        private const val ROOM_COUNTER_KEY = "roomCounter"
        private const val ROOM_KEY_PREFIX = "room:"
        private const val ROOM_KEY_PATTERN = "$ROOM_KEY_PREFIX*"
        private const val ID_QUEUE_INITIAL_ID = 2L
        private const val ID_QUEUE_LIMIT_ID = 10L
        private const val MEMBERS_INITIAL_ID = 1L
    }
    private val logger = KotlinLogging.logger { }
    private val addMemberLua = """
        local limitNumberOfMember = 10
        local room = cjson.decode(redis.call("get", KEYS[1]))
        if not room then
            error("Room not found")
        end
        if #room.members == limitNumberOfMember then
            error("Full room")
        end
        local memberId = table.remove(room["idQueue"], 1)
        table.insert(room["members"], memberId)
        redis.call("set", KEYS[1], cjson.encode(room))
        return memberId
    """

    override fun save(name: String): Room {
        val generatedId = generateId()
        val roomToSave = Room(
            generatedId,
            name,
            (ID_QUEUE_INITIAL_ID..ID_QUEUE_LIMIT_ID).toMutableList(),
            mutableListOf(MEMBERS_INITIAL_ID)
        )
        createBucket(ROOM_KEY_PREFIX + generatedId).set(mapper.getStringValue(roomToSave))
        return roomToSave
    }

    override fun findById(id: Long): Room? {
        return createBucket(ROOM_KEY_PREFIX + id).get()?.let { mapper.getExpectedValue(it, Room::class.java) }
    }

    override fun getById(id: Long) =
        createBucket(ROOM_KEY_PREFIX + id).get()?.let {
            mapper.getExpectedValue(it, Room::class.java)
        } ?: throw RoomNotFoundException()

    override fun findAll(): List<Room> =
        redisson.run {
            keys.getKeys(KeysScanParams().pattern(ROOM_KEY_PATTERN)).mapNotNull {
                mapper.getExpectedValue(createBucket(it).get(), Room::class.java)
            }.toList()
        }

    override fun deleteAll() {
        redisson.keys.run {
            delete(ROOM_COUNTER_KEY)
            deleteByPattern(ROOM_KEY_PATTERN)
        }
    }

    override fun addMember(id: Long): Long {
        return redisson.getScript(LongCodec.INSTANCE).eval(
            RScript.Mode.READ_WRITE,
            addMemberLua,
            RScript.ReturnType.INTEGER,
            listOf(ROOM_KEY_PREFIX + id)
        )
    }

    override fun deleteMember(id: Long, memberId: Long): Room? {
        TODO("Not yet implemented")
    }

    private fun generateId() = redisson.getAtomicLong(ROOM_COUNTER_KEY).incrementAndGet()

    private fun createBucket(key: String) = redisson.getBucket<String>(key, StringCodec.INSTANCE)
}