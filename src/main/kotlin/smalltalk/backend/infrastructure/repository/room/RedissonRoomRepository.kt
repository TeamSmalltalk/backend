package smalltalk.backend.infrastructure.repository.room

import io.github.oshai.kotlinlogging.KotlinLogging
import org.redisson.api.FunctionMode
import org.redisson.api.FunctionResult
import org.redisson.api.RedissonClient
import org.redisson.api.options.KeysScanParams
import org.springframework.stereotype.Repository
import smalltalk.backend.domain.room.Room
import smalltalk.backend.exception.room.advice.RoomExceptionSituationCode.*
import smalltalk.backend.exception.room.situation.FullRoomException
import smalltalk.backend.exception.room.situation.RoomNotFoundException
import smalltalk.backend.infrastructure.repository.ObjectMapperClient

@Repository
class RedissonRoomRepository(
    private val redisson: RedissonClient,
    private val objectMapper: ObjectMapperClient,
    properties: RoomProperties
) : RoomRepository {
    private val logger = KotlinLogging.logger { }
    private val keyPrefix = properties.getKeyPrefix()
    private val keyOfCounter = properties.getKeyOfCounter()
    private val keyPostfixOfProvider = properties.getKeyPostfixOfProvider()
    private val functionKeyOfAddMember = properties.getLibraryFunctionKeyOfAddMember()
    private val functionKeyOfDeleteMember = properties.getLibraryFunctionKeyOfDeleteMember()
    private val keyPattern = "$keyPrefix*[^a-z]"
    private val initNumberOfMember = properties.getInitNumberOfMember()
    private val limitNumberOfMember = properties.getLimitNumberOfMember()

    override fun save(name: String): Room {
        val generatedId = generateId()
        val roomToSave = Room(generatedId, name, initNumberOfMember)
        redisson.getBucket<String>(keyPrefix + generatedId).set(objectMapper.getStringValue(roomToSave))
        return roomToSave
    }

    override fun findById(id: Long) = findByKey(keyPrefix + id)

    override fun findAll() = redisson.keys.getKeys(KeysScanParams().pattern(keyPattern)).mapNotNull { findByKey(it) }

    override fun deleteAll() {
        redisson.keys.flushdb()
    }

    /**
     * KEYS[1] = keyPrefix + id
     * KEYS[2] = keyPrefix + id + keyPostfixOfProvider
     * ARGV[1] = limitNumberOfMember
     */
    override fun addMember(id: Long) =
        when (
            val functionReturnValue = redisson.function.call<String>(
                FunctionMode.WRITE,
                functionKeyOfAddMember,
                FunctionResult.STRING,
                (keyPrefix + id).let { listOf(it, it + keyPostfixOfProvider) },
                limitNumberOfMember.toString()
            )
        ) {
            DELETED.code -> throw RoomNotFoundException()
            FULL.code -> throw FullRoomException()
            else -> functionReturnValue.toLong()
        }

    /**
     * KEYS[1] = keyPrefix + id
     * KEYS[2] = keyPrefix + id + keyPostfixOfProvider
     * ARGV[1] = initNumberOfMember
     */
    override fun deleteMember(id: Long, memberId: Long) =
        redisson.function.call<String>(
            FunctionMode.WRITE,
            functionKeyOfDeleteMember,
            FunctionResult.STRING,
            (keyPrefix + id).let { listOf(it, it + keyPostfixOfProvider) },
            initNumberOfMember.toString()
        )?.let {
            when (it) {
                DELETED.code -> throw RoomNotFoundException()
                else -> getExpectedValue<Room>(it)
            }
        }

    private fun generateId() = redisson.getAtomicLong(keyOfCounter).incrementAndGet()

    private fun findByKey(key: String) =
        redisson.getBucket<String>(key).get()?.let { value ->
            getExpectedValue<Room>(value).let { room ->
                Room(room.id, room.name, room.numberOfMember)
            }
        }

    private inline fun <reified T : Any> getExpectedValue(value: Any) = objectMapper.getExpectedValue(value, T::class.java)
}