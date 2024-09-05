package smalltalk.backend.infra.repository.room

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.connection.RedisConnection
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import smalltalk.backend.domain.room.Room
import smalltalk.backend.exception.room.situation.FullRoomException
import smalltalk.backend.exception.room.situation.RoomIdNotGeneratedException
import smalltalk.backend.exception.room.situation.RoomNotFoundException


@Repository
class RedisRoomRepository(
    private val template: StringRedisTemplate,
    private val mapper: ObjectMapper
) : RoomRepository {
    private val logger = KotlinLogging.logger { }
    companion object {
        private const val ID_QUEUE_INITIAL_ID = 1L
        private const val ID_QUEUE_LIMIT_ID = 10L
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
                mutableListOf()
            )
        template.opsForValue()[ROOM_KEY_PREFIX + generatedRoomId] = convertValueToString(room)
        return room
    }

    override fun getById(roomId: Long) =
        findByKey(ROOM_KEY_PREFIX + roomId) ?: throw RoomNotFoundException()

    override fun findAll() =
        findKeysByPattern().mapNotNull { findByKey(it) }

    override fun deleteAll() {
        template.run {
            delete(ROOM_COUNTER_KEY)
            delete(findKeysByPattern())
        }
    }

    override fun addMember(roomId: Long): Long {
        val key = (ROOM_KEY_PREFIX + roomId).toByteArray()
        var memberId = 0L
        do {
            val transactionResults =
                template.execute {
                    return@execute it.apply {
                        watch(key)
                        val room = getByKey(key, it)
                        checkFull(room)
                        multi()
                        stringCommands()[key] =
                            convertValueToByteArray(
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

    override fun deleteMember(roomId: Long, memberId: Long) {
        val key = (ROOM_KEY_PREFIX + roomId).toByteArray()
        do {
            val transactionResults =
                template.execute {
                    return@execute it.apply {
                        watch(key)
                        val room = getByKey(key, it)
                        multi()
                        if (checkLastMember(room))
                            stringCommands().getDel(key)
                        else {
                            stringCommands()[key] =
                                convertValueToByteArray(
                                    room.apply {
                                        members.remove(memberId)
                                        idQueue.add(memberId)
                                    }
                                )
                        }
                    }.exec()
                }
        } while (transactionResults.isNullOrEmpty())
    }

    private fun generateRoomId() =
        template.opsForValue().increment(ROOM_COUNTER_KEY) ?: throw RoomIdNotGeneratedException()

    private fun findByKey(key: String) =
        template.opsForValue()[key]?.let { convertValueToRoom(it) }

    private fun getByKey(key: ByteArray, connection: RedisConnection) =
        connection.stringCommands()[key]?.let { convertValueToRoom(it) } ?: throw RoomNotFoundException()

    private fun findKeysByPattern() =
        template.keys(ROOM_KEY_PATTERN)

    private fun convertValueToString(value: Room) =
        mapper.writeValueAsString(value)

    private fun convertValueToByteArray(value: Room) =
        mapper.writeValueAsBytes(value)

    private fun convertValueToRoom(value: Any) =
        when (value) {
            is String -> mapper.readValue(value, Room::class.java)
            is ByteArray -> mapper.readValue(value, Room::class.java)
            else -> throw IllegalStateException("Not allowed type")
        }

    private fun checkFull(room: Room) {
        if (room.members.size == 10)
            throw FullRoomException()
    }

    private fun checkLastMember(room: Room) =
        room.members.size == 1
}