package smalltalk.backend.infrastructure.repository.room

import io.github.oshai.kotlinlogging.KotlinLogging
import org.redisson.api.RScript.Mode
import org.redisson.api.RScript.ReturnType
import org.redisson.api.RedissonClient
import org.redisson.api.options.KeysScanParams
import org.springframework.stereotype.Repository
import smalltalk.backend.domain.room.Room
import smalltalk.backend.exception.room.advice.RoomExceptionSituationCode.*
import smalltalk.backend.exception.room.situation.FullRoomException
import smalltalk.backend.exception.room.situation.RoomNotFoundException
import smalltalk.backend.util.jackson.ObjectMapperClient

@Repository
class RedissonRoomRepository(
    private val redisson: RedissonClient,
    private val objectMapper: ObjectMapperClient,
    private val scriptLoader: RedisLuaLoader
) : RoomRepository {
    companion object {
        private const val KEY_PREFIX = "room:"
        private const val COUNTER_KEY = "${KEY_PREFIX}counter"
        private const val PROVIDER_KEY_POSTFIX = ":provider"
        private const val KEY_PATTERN = "$KEY_PREFIX*[^a-z]"
        private const val MEMBER_INIT = 1
        private const val MEMBER_LIMIT = 100
    }
    private val logger = KotlinLogging.logger { }

    override fun save(name: String): Room {
        val generatedId = generateId()
        val roomToSave = Room(generatedId, name, MEMBER_INIT)
        redisson.getBucket<String>(KEY_PREFIX + generatedId).set(objectMapper.getStringValue(roomToSave))
        return roomToSave
    }

    override fun findById(id: Long) = findByKey(KEY_PREFIX + id)

    override fun findAll(): List<Room> = redisson.keys.getKeys(KeysScanParams().pattern(KEY_PATTERN)).mapNotNull { findByKey(it) }

    override fun deleteAll() {
        redisson.keys.flushdb()
    }

    /**
     * KEYS[1] = "room:{id}"
     * KEYS[2] = "room:{id}:provider"
     * ARGV[1] = MEMBER_LIMIT
     */
    override fun addMember(id: Long) =
        when (
            val scriptReturnValue = redisson.script.evalSha<String>(
                Mode.READ_WRITE,
                scriptLoader.getShaCode(RedisLuaLoader.ROOM_ADD_MEMBER_KEY),
                ReturnType.VALUE,
                (KEY_PREFIX + id).let { listOf(it, it + PROVIDER_KEY_POSTFIX) },
                MEMBER_LIMIT.toString()
            )
        ) {
            DELETED.code -> throw RoomNotFoundException()
            FULL.code -> throw FullRoomException()
            else -> scriptReturnValue.toLong()
        }

    /**
     * KEYS[1] = "room:{id}"
     * KEYS[2] = "room:{id}:provider"
     * ARGV[1] = memberId
     */
    override fun deleteMember(id: Long, memberId: Long) =
        redisson.script.evalSha<String>(
            Mode.READ_WRITE,
            scriptLoader.getShaCode(RedisLuaLoader.ROOM_DELETE_MEMBER_KEY),
            ReturnType.VALUE,
            (KEY_PREFIX + id).let { listOf(it, it + PROVIDER_KEY_POSTFIX) },
            memberId.toString()
        )?.let {
            when (it) {
                DELETED.code -> throw RoomNotFoundException()
                else -> getExpectedValue<Room>(it)
            }
        }

    private fun generateId() = redisson.getAtomicLong(COUNTER_KEY).incrementAndGet()

    private fun findByKey(key: String) =
        redisson.getBucket<String>(key).get()?.let { value ->
            getExpectedValue<Room>(value).let { room ->
                Room(room.id, room.name, room.numberOfMember)
            }
        }

    private inline fun <reified T : Any> getExpectedValue(value: Any) = objectMapper.getExpectedValue(value, T::class.java)
}