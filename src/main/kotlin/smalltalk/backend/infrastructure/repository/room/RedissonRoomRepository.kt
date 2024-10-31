package smalltalk.backend.infrastructure.repository.room

import io.github.oshai.kotlinlogging.KotlinLogging
import org.redisson.api.RedissonClient
import org.redisson.api.options.KeysScanParams
import org.redisson.client.codec.LongCodec
import org.redisson.client.codec.StringCodec
import org.springframework.stereotype.Repository
import smalltalk.backend.domain.room.Room
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
        val roomToSave = Room(generatedId, name, MEMBER_INIT.toInt())
        createRBucket(KEY_PREFIX + generatedId).set(objectMapper.getStringValue(roomToSave))
        redisson.run {
            createRList(KEY_PREFIX + generatedId + MEMBER_KEY_POSTFIX).add(MEMBER_INIT)
            createRList(KEY_PREFIX + generatedId + PROVIDER__KEY_POSTFIX).addAll((PROVIDER_INIT..PROVIDER_LIMIT).toList())
        }
        return roomToSave
    }

    override fun findById(id: Long) = findByKey(KEY_PREFIX + id)

    override fun findAll(): List<Room> = redisson.keys.getKeys(KeysScanParams().pattern(KEY_PATTERN)).mapNotNull { findByKey(it) }

    override fun deleteAll() {
        redisson.keys.flushdb()
    }

    override fun addMember(id: Long): Long {
//        return redisson.getScript(LongCodec.INSTANCE).eval(
//            RScript.Mode.READ_WRITE,
//            addMemberLua,
//            RScript.ReturnType.INTEGER,
//            listOf(ROOM_KEY_PREFIX + id)
//        )
        TODO("Not yet implemented")
    }

    override fun deleteMember(id: Long, memberId: Long): Room? {
        TODO("Not yet implemented")
    }

    private fun generateId() = redisson.getAtomicLong(COUNTER_KEY).incrementAndGet()

    private fun createRBucket(key: String) = redisson.getBucket<String>(key, StringCodec.INSTANCE)

    private fun createRList(key: String) = redisson.getList<Long>(key, LongCodec.INSTANCE)

    /**
     * key[0] -> RBucket
     * key[1] -> RList
     */
    private fun findByKey(key: String) =
        createRBucket(key).get()?.let { value ->
            getExpectedValue<Room>(value).let { room ->
                Room(room.id, room.name, room.numberOfMember)
            }
        }

    private inline fun <reified T : Any> getExpectedValue(value: Any) = objectMapper.getExpectedValue(value, T::class.java)
}