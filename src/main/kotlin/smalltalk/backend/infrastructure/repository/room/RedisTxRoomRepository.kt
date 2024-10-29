package smalltalk.backend.infrastructure.repository.room

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.connection.RedisConnection
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import smalltalk.backend.domain.room.Room
import smalltalk.backend.exception.room.situation.FullRoomException
import smalltalk.backend.exception.room.situation.RoomIdNotGeneratedException
import smalltalk.backend.exception.room.situation.RoomNotFoundException
import smalltalk.backend.util.jackson.ObjectMapperClient

@Repository
class RedisTxRoomRepository(
    private val template: StringRedisTemplate,
    private val client: ObjectMapperClient
) : RoomRepository {
    private val logger = KotlinLogging.logger { }
    private val operations = template.opsForValue()
    companion object {
        private const val ID_QUEUE_INITIAL_ID = 2L
        private const val ID_QUEUE_LIMIT_ID = 10L
        private const val MEMBERS_INITIAL_ID = 1L
        private const val ROOM_COUNTER_KEY = "roomCounter"
        private const val ROOM_KEY_PREFIX = "room:"
        private const val ROOM_KEY_PATTERN = "$ROOM_KEY_PREFIX*"
    }

    override fun save(name: String): Room {
        val generatedId = generateId()
        val roomToSave = Room(
                generatedId,
                name,
                (ID_QUEUE_INITIAL_ID..ID_QUEUE_LIMIT_ID).toMutableList(),
                mutableListOf(MEMBERS_INITIAL_ID)
        )
        operations[ROOM_KEY_PREFIX + generatedId] = client.getStringValue(roomToSave)
        return roomToSave
    }

    override fun findById(id: Long) = findByKey(ROOM_KEY_PREFIX + id)

    override fun getById(id: Long) = findByKey(ROOM_KEY_PREFIX + id) ?: throw RoomNotFoundException()

    override fun findAll() = findKeys().mapNotNull { findByKey(it) }

    override fun deleteAll() {
        template.run {
            delete(ROOM_COUNTER_KEY)
            delete(findKeys())
        }
    }

    override fun addMember(id: Long): Long {
        val key = (ROOM_KEY_PREFIX + id).toByteArray()
        var memberId = 0L
        do {
            val transactionResults =
                template.execute {
                    return@execute it.apply {
                        watch(key)
                        val room = getByKey(key, it)
                        checkFull(room)
                        multi()
                        stringCommands()[key] = client.getByteArrayValue(
                            room.apply {
                                memberId = idQueue.removeFirst()
                                members.add(memberId)
                            }
                        )
                    }.exec()
                }
        } while (transactionResults.isNullOrEmpty())
        return memberId
    }

    override fun deleteMember(id: Long, memberId: Long): Room? {
        var room: Room? = null
        val key = (ROOM_KEY_PREFIX + id).toByteArray()
        do {
            val transactionResults =
                template.execute {
                    return@execute it.apply {
                        watch(key)
                        val roomToCheck = getByKey(key, it)
                        multi()
                        if (checkLastMember(roomToCheck)) {
                            room = null
                            stringCommands().getDel(key)
                        }
                        else {
                            roomToCheck.apply {
                                members.remove(memberId)
                                idQueue.add(memberId)
                            }.let { updatedRoom ->
                                room = updatedRoom
                                stringCommands()[key] = client.getByteArrayValue(updatedRoom)
                            }
                        }
                    }.exec()
                }
        } while (transactionResults.isNullOrEmpty())
        return room
    }

    private fun generateId() = operations.increment(ROOM_COUNTER_KEY) ?: throw RoomIdNotGeneratedException()

    private fun findByKey(key: String) = operations[key]?.let { client.getExpectedValue(it, Room::class.java) }

    private fun getByKey(key: ByteArray, connection: RedisConnection) =
        connection.stringCommands()[key]?.let { client.getExpectedValue(it, Room::class.java) } ?: throw RoomNotFoundException()

    private fun findKeys() = template.keys(ROOM_KEY_PATTERN)

    private fun checkFull(room: Room) {
        if (room.members.size == 10)
            throw FullRoomException()
    }

    private fun checkLastMember(room: Room) = (room.members.size == 1)
}