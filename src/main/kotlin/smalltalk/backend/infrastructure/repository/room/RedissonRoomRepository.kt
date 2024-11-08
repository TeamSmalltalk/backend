package smalltalk.backend.infrastructure.repository.room

import io.github.oshai.kotlinlogging.KotlinLogging
import org.redisson.api.RScript.*
import org.redisson.api.RedissonClient
import org.redisson.api.options.KeysScanParams
import org.redisson.client.codec.StringCodec
import org.springframework.stereotype.Repository
import smalltalk.backend.domain.room.Room
import smalltalk.backend.exception.room.situation.FullRoomException
import smalltalk.backend.exception.room.situation.RoomNotFoundException
import smalltalk.backend.util.jackson.ObjectMapperClient

@Repository
class RedissonRoomRepository(
    private val redisson: RedissonClient,
    private val objectMapper: ObjectMapperClient
) : RoomRepository {
    companion object {
        private const val KEY_PREFIX = "room:"
        private const val COUNTER_KEY = "${KEY_PREFIX}counter"
        private const val MEMBER_KEY_POSTFIX = ":member"
        private const val PROVIDER__KEY_POSTFIX = ":provider"
        private const val KEY_PATTERN = "$KEY_PREFIX*[^a-z]"
        private const val MEMBER_INIT = 1L
        private const val PROVIDER_INIT = 2L
        private const val PROVIDER_LIMIT = 10L
    }
    private val logger = KotlinLogging.logger { }
    private val addMemberLua = """
        local value = redis.call("get", KEYS[1])
        if not value then
            return "601"
        end
        local room = cjson.decode(value)
        if room.numberOfMember == tonumber(ARGV[1]) then
            return "602"
        end
        local memberId = redis.call("lpop", KEYS[3])
        redis.call("rpush", KEYS[2], memberId)
        room.numberOfMember = room.numberOfMember + 1
        redis.call("set", KEYS[1], cjson.encode(room))
        return memberId
    """
    private val deleteMemberLua = """
        local value = redis.call("get", KEYS[1])
        if not value then
            return "601"
        end
        local room = cjson.decode(value)
        if room.numberOfMember == 1 then
            redis.call("del", KEYS[1], KEYS[2], KEYS[3])
            return nil
        end
        redis.call("lrem", KEYS[2], "1", ARGV[1])
        redis.call("rpush", KEYS[3], ARGV[1])
        room.numberOfMember = room.numberOfMember - 1
        redis.call("set", KEYS[1], cjson.encode(room))
        return cjson.encode(room)
    """

    override fun save(name: String): Room {
        val generatedId = generateId()
        val roomToSave = Room(generatedId, name, MEMBER_INIT.toInt())
        val key = KEY_PREFIX + generatedId
        createRBucket(key).set(objectMapper.getStringValue(roomToSave))
        redisson.run {
            createRList(key + MEMBER_KEY_POSTFIX).add(MEMBER_INIT.toString())
            createRList(key + PROVIDER__KEY_POSTFIX).addAll((PROVIDER_INIT..PROVIDER_LIMIT).map { it.toString() })
        }
        return roomToSave
    }

    override fun findById(id: Long) = findByKey(KEY_PREFIX + id)

    override fun findAll(): List<Room> = redisson.keys.getKeys(KeysScanParams().pattern(KEY_PATTERN)).mapNotNull { findByKey(it) }

    override fun deleteAll() {
        redisson.keys.flushdb()
    }

    /**
     * KEYS[1] = "room:{id}"
     * KEYS[2] = "room:{id}:member"
     * KEYS[3] = "room:{id}:provider"
     * ARGV[1] = PROVIDER_LIMIT
     */
    override fun addMember(id: Long): Long {
        val key = KEY_PREFIX + id
        val scriptReturnValue = redisson.getScript(StringCodec.INSTANCE).eval<String>(
            Mode.READ_WRITE,
            addMemberLua,
            ReturnType.VALUE,
            listOf(key, key + MEMBER_KEY_POSTFIX, key + PROVIDER__KEY_POSTFIX),
            PROVIDER_LIMIT.toString()
        )
        return when (scriptReturnValue) {
            "601" -> throw RoomNotFoundException()
            "602" -> throw FullRoomException()
            else -> scriptReturnValue.toLong()
        }
    }

    /**
     * KEYS[1] = "room:{id}"
     * KEYS[2] = "room:{id}:member"
     * KEYS[3] = "room:{id}:provider"
     * ARGV[1] = memberId
     */
    override fun deleteMember(id: Long, memberId: Long): Room? {
        val key = KEY_PREFIX + id
        val scriptReturnValue = redisson.getScript(StringCodec.INSTANCE).eval<String>(
            Mode.READ_WRITE,
            deleteMemberLua,
            ReturnType.VALUE,
            listOf(key, key + MEMBER_KEY_POSTFIX, key + PROVIDER__KEY_POSTFIX),
            memberId.toString()
        )
        return scriptReturnValue?.let {
            when (it) {
                "601" -> throw RoomNotFoundException()
                else -> getExpectedValue<Room>(it)
            }
        }
    }

    private fun generateId() = redisson.getAtomicLong(COUNTER_KEY).incrementAndGet()

    private fun createRBucket(key: String) = redisson.getBucket<String>(key, StringCodec.INSTANCE)

    private fun createRList(key: String) = redisson.getList<String>(key, StringCodec.INSTANCE)

    private fun findByKey(key: String) =
        createRBucket(key).get()?.let { value ->
            getExpectedValue<Room>(value).let { room ->
                Room(room.id, room.name, room.numberOfMember)
            }
        }

    private inline fun <reified T : Any> getExpectedValue(value: Any) = objectMapper.getExpectedValue(value, T::class.java)
}