package smalltalk.backend.infrastructure.repository.room

import smalltalk.backend.domain.room.Room
import java.math.BigInteger

interface RoomRepository {
    fun save(roomName: String): BigInteger?
    fun findById(roomId: BigInteger): Room?
    fun findAll(): List<Room>
    fun addMember(room: Room): Room
    fun deleteMember(roomId: BigInteger, memberId: BigInteger)
    fun updateRoom(room: Room)
    fun deleteById(roomId: BigInteger): Boolean
    fun deleteAll(): Long
}