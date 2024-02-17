package smalltalk.backend.infrastructure.repository.room

import smalltalk.backend.domain.room.Room
import java.math.BigInteger

interface RoomRepository {
    fun save(roomName: String): BigInteger?
    fun findById(roomId: BigInteger): Room?
    fun findAll(): List<Room>
    fun addMember(room: Room): Room
    fun deleteMember(room: Room, memberId: Int): Room
    fun update(updatedRoom: Room)
    fun deleteById(roomId: BigInteger)
    fun deleteAll()
}