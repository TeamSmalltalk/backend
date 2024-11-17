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
        private const val PROVIDER_KEY_POSTFIX = ":provider"
        private const val FIND_KEY_PATTERN = "$KEY_PREFIX*[^a-z]"
        private const val DELETE_KEY_PATTERN = "$KEY_PREFIX*"
        private const val MEMBER_INIT = 1
        private const val MEMBER_LIMIT = 100
    }
    private val logger = KotlinLogging.logger { }
    private val valueOperations = redisTemplate.opsForValue()

    override fun save(name: String): Room {
        val generatedId = generateId()
        val roomToSave = Room(generatedId, name, MEMBER_INIT)
        valueOperations[KEY_PREFIX + generatedId] = objectMapper.getStringValue(roomToSave)
        return roomToSave
    }

    override fun findById(id: Long) = findByKey(KEY_PREFIX + id)

    override fun findAll() = findKeysByPattern(FIND_KEY_PATTERN).mapNotNull { findByKey(it) }

    override fun deleteAll() {
        redisTemplate.delete(findKeysByPattern(DELETE_KEY_PATTERN))
    }

    override fun addMember(id: Long): Long {
        val key = KEY_PREFIX + id
        val byteKey = key.toByteArray()
        val byteKeyOfProvider = (key + PROVIDER_KEY_POSTFIX).toByteArray()
        var memberId = 0L
        do {
            val transactionResults = redisTemplate.execute { connection ->
                return@execute connection.apply {
                    watch(byteKey, byteKeyOfProvider)
                    val room = getExpectedValue<Room>(stringCommands()[byteKey] ?: throw RoomNotFoundException())
                    val values = listCommands().lRange(byteKeyOfProvider, 0, 0)
                    checkFull(room)
                    multi()
                    memberId = (room.numberOfMember + 1).toLong()
                    stringCommands()[byteKey] = objectMapper.getByteArrayValue(Room(room.id, room.name, memberId.toInt()))
                    if (!values.isNullOrEmpty()) {
                        listCommands().lPop(byteKeyOfProvider)
                        memberId = getExpectedValue<Long>(values[0])
                    }
                }.exec()
            }
        } while (transactionResults.isNullOrEmpty())
        return memberId
    }

    override fun deleteMember(id: Long, memberId: Long): Room? {
        var room: Room? = null
        val key = KEY_PREFIX + id
        val byteKey = key.toByteArray()
        val byteKeyOfProvider = (key + PROVIDER_KEY_POSTFIX).toByteArray()
        do {
            val transactionResults = redisTemplate.execute {
                return@execute it.apply {
                    watch(byteKey, byteKeyOfProvider)
                    val foundRoom = getExpectedValue<Room>(stringCommands()[byteKey] ?: throw RoomNotFoundException())
                    multi()
                    if (checkLastMember(foundRoom)) {
                        keyCommands().del(byteKey, byteKeyOfProvider)
                        room = null
                    }
                    else {
                        val updatedRoom = Room(foundRoom.id, foundRoom.name, foundRoom.numberOfMember - 1)
                        stringCommands()[byteKey] = objectMapper.getByteArrayValue(updatedRoom)
                        listCommands().rPushX(byteKeyOfProvider, objectMapper.getByteArrayValue(id))
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
        if (room.numberOfMember == MEMBER_LIMIT)
            throw FullRoomException()
    }

    private fun checkLastMember(room: Room) = (room.numberOfMember == MEMBER_INIT)
}