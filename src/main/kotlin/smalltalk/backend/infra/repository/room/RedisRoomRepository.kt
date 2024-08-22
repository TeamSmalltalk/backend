package smalltalk.backend.infra.repository.room

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import smalltalk.backend.domain.room.Room
import smalltalk.backend.infra.exception.room.situation.FullRoomException
import smalltalk.backend.infra.exception.room.situation.RoomIdNotGeneratedException
import smalltalk.backend.infra.exception.room.situation.RoomNotFoundException

@Repository
class RedisRoomRepository(
    private val template: StringRedisTemplate,
    private val mapper: ObjectMapper
) : RoomRepository {
    companion object {
        private const val ID_QUEUE_INITIAL_ID = 2L
        private const val ID_QUEUE_LIMIT_ID = 10L
        private const val MEMBERS_INITIAL_ID = 1L
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
                mutableListOf(MEMBERS_INITIAL_ID)
            )
        template.opsForValue()[ROOM_KEY_PREFIX + generatedRoomId] = convertToStringValue(room)
        return room
    }

    override fun getById(roomId: Long) =
        findByKey(ROOM_KEY_PREFIX + roomId) ?: throw RoomNotFoundException()

    override fun findAll() =
        findKeysByPattern().mapNotNull { findByKey(it) }

    override fun deleteById(roomId: Long) {
    }

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
                        val room =
                            stringCommands()[key]?.let { byteArrayRoom ->
                                convertValueToRoom(byteArrayRoom)
                            } ?: throw RoomNotFoundException()
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

    override fun deleteMember(room: Room, memberId: Long) {
    }

    private fun generateRoomId() =
        template.opsForValue().increment(ROOM_COUNTER_KEY) ?: throw RoomIdNotGeneratedException()

    private fun findByKey(key: String) =
        template.opsForValue()[key]?.let { convertValueToRoom(it) }

    private fun findKeysByPattern() =
        template.keys(ROOM_KEY_PATTERN)

    private fun convertToStringValue(value: Room) =
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
}