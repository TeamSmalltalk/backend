package smalltalk.backend.infrastructure.repository.room

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.ScanOptions
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import smalltalk.backend.domain.room.Room
import smalltalk.backend.exception.room.situation.FullRoomException
import smalltalk.backend.exception.room.situation.RoomIdNotGeneratedException
import smalltalk.backend.exception.room.situation.RoomNotFoundException
import smalltalk.backend.util.jackson.ObjectMapperClient

//@Repository
class LettuceRoomRepository(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapperClient
) : RoomRepository {
    companion object {
        private const val KEY_PREFIX = "room:"
        private const val COUNTER_KEY = "${KEY_PREFIX}counter"
        private const val MEMBER_KEY_POSTFIX = ":member"
        private const val PROVIDER__KEY_POSTFIX = ":provider"
        private const val FIND_KEY_PATTERN = "$KEY_PREFIX*[^a-z]"
        private const val DELETE_KEY_PATTERN = "$KEY_PREFIX*"
        private const val MEMBER_INIT = 1L
        private const val PROVIDER_INIT = 2L
        private const val PROVIDER_LIMIT = 10L
    }
    private val logger = KotlinLogging.logger { }
    private val valueOperations = redisTemplate.opsForValue()
    private val listOperations = redisTemplate.opsForList()

    override fun save(name: String): Room {
        val generatedId = generateId()
        val roomToSave = Room(generatedId, name, MEMBER_INIT.toInt())
        valueOperations[KEY_PREFIX + generatedId] = objectMapper.getStringValue(roomToSave)
        listOperations.run {
            leftPush(KEY_PREFIX + generatedId + MEMBER_KEY_POSTFIX, MEMBER_INIT.toString())
            leftPushAll(
                KEY_PREFIX + generatedId + PROVIDER__KEY_POSTFIX,
                (PROVIDER_INIT..PROVIDER_LIMIT).map { it.toString() }
            )
        }
        return roomToSave
    }

    override fun findById(id: Long) = findByKey(KEY_PREFIX + id)

    override fun findAll() = findKeysByPattern(FIND_KEY_PATTERN).mapNotNull { findByKey(it) }

    override fun deleteAll() {
        redisTemplate.delete(findKeysByPattern(DELETE_KEY_PATTERN))
    }

    override fun addMember(id: Long): Long {
        val key = (KEY_PREFIX + id).toByteArray()
        val keyOfMember = (KEY_PREFIX + id + MEMBER_KEY_POSTFIX).toByteArray()
        val keyOfProvider = (KEY_PREFIX + id + PROVIDER__KEY_POSTFIX).toByteArray()
        var memberId = 0L
        do {
            val transactionResults = redisTemplate.execute {
                return@execute it.apply {
                    watch(key, keyOfMember, keyOfProvider)
                    val room = getExpectedValue<Room>(stringCommands()[key] ?: throw RoomNotFoundException())
                    checkFull(room)
                    val element = listCommands().lRange(keyOfProvider, 0, 0)?.get(0)
                        ?: throw IllegalStateException("Doesnt exist id to add")
                    multi()
                    stringCommands()[key] = objectMapper.getByteArrayValue(
                        Room(room.id, room.name, room.numberOfMember + 1)
                    )
                    listCommands().run {
                        lPop(keyOfProvider)
                        rPushX(keyOfMember, element)
                    }
                    memberId = getExpectedValue<Long>(element)
                }.exec()
            }
        } while (transactionResults.isNullOrEmpty())
        return memberId
    }

    override fun deleteMember(id: Long, memberId: Long): Room? {
        var room: Room? = null
        val key = (KEY_PREFIX + id).toByteArray()
        val keyOfMember = (KEY_PREFIX + id + MEMBER_KEY_POSTFIX).toByteArray()
        val keyOfProvider = (KEY_PREFIX + id + PROVIDER__KEY_POSTFIX).toByteArray()
        do {
            val transactionResults = redisTemplate.execute {
                return@execute it.apply {
                    watch(key, keyOfMember, keyOfProvider)
                    val foundRoom = getExpectedValue<Room>(stringCommands()[key] ?: throw RoomNotFoundException())
                    multi()
                    if (checkLastMember(foundRoom)) {
                        keyCommands().del(key, keyOfMember, keyOfProvider)
                        room = null
                    }
                    else {
                        val updatedRoom = Room(foundRoom.id, foundRoom.name, foundRoom.numberOfMember - 1)
                        val value = objectMapper.getByteArrayValue(id)
                        stringCommands()[key] = objectMapper.getByteArrayValue(updatedRoom)
                        listCommands().run {
                            lRem(keyOfMember, 1, value)
                            rPushX(keyOfProvider, value)
                        }
                        room = updatedRoom
                    }
                }.exec()
            }
        } while (transactionResults.isNullOrEmpty())
        return room
    }

    private fun generateId() = valueOperations.increment(COUNTER_KEY) ?: throw RoomIdNotGeneratedException()

    private fun findByKey(key: String) =
        valueOperations[key]?.let { value ->
            getExpectedValue<Room>(value).let { room ->
                Room(room.id, room.name, room.numberOfMember)
            }
        }

    private fun findKeysByPattern(pattern: String) =
        redisTemplate.scan(ScanOptions.scanOptions().match(pattern).build()).iterator().asSequence().toList()

    private inline fun <reified T : Any> getExpectedValue(value: Any) = objectMapper.getExpectedValue(value, T::class.java)

    private fun checkFull(room: Room) {
        if (room.numberOfMember == 10)
            throw FullRoomException()
    }

    private fun checkLastMember(room: Room) = (room.numberOfMember == 1)
}