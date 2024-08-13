package smalltalk.backend.infrastructure.repository.room

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Repository
import smalltalk.backend.application.exception.room.situation.RoomIdNotGeneratedException
import smalltalk.backend.domain.room.Room

@Repository
class RedisRoomRepository(
    private val template: StringRedisTemplate,
    private val objectMapper: ObjectMapper
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
        template.opsForValue()[ROOM_KEY_PREFIX + generatedRoomId] = convertTypeToString(room)
        return room
    }

    override fun findById(roomId: Long): Room? = findByKey(ROOM_KEY_PREFIX + roomId)

    override fun findAll() =
        findKeysByPattern(ROOM_KEY_PATTERN).mapNotNull {
            findByKey(it)
        }

    override fun addMember(room: Room): Long {
        val key = (ROOM_KEY_PREFIX + room.id).toByteArray()
        var memberId = 0L
        if (
            template.execute {
                return@execute it.apply {
                    watch(key)
                    multi()
                    stringCommands().set(
                        key,
                        convertTypeToString(
                            room.apply {
                                memberId = idQueue.removeFirst()
                                members.add(memberId)
                            }
                        ).toByteArray()
                    )
                }.exec()
            }.isNullOrEmpty()
        ) {
            memberId = 0L
        }
        return memberId
    }

    override fun deleteMember(room: Room, memberId: Long) {
        template.opsForValue()[ROOM_KEY_PREFIX + room.id] =
            convertTypeToString(
                room.apply {
                    members.remove(memberId)
                    idQueue.add(memberId)
                }
            )
    }

    override fun deleteByRoom(room: Room) {
        val key = (ROOM_KEY_PREFIX + room.id).toByteArray()
        template.execute {
            return@execute it.apply {
                watch(key)
                multi()
                stringCommands().getDel(key)
            }.exec()
        }
    }

    override fun deleteAll() {
        template.run {
            delete(ROOM_COUNTER_KEY)
            delete(findKeysByPattern(ROOM_KEY_PATTERN))
        }
    }

    private fun generateRoomId() =
        template.opsForValue().increment(ROOM_COUNTER_KEY) ?: throw RoomIdNotGeneratedException()

    private fun findByKey(key: String) =
        template.opsForValue()[key]?.let {
            objectMapper.readValue(it, Room::class.java)
        }

    private fun findKeysByPattern(key: String) = template.keys(key)

    private fun convertTypeToString(room: Room) = objectMapper.writeValueAsString(room)
}