package smalltalk.backend.application.implement.room

import smalltalk.backend.application.websocket.SystemType
import smalltalk.backend.domain.member.Member
import smalltalk.backend.domain.room.Room
import smalltalk.backend.presentation.dto.room.response.Enter
import smalltalk.backend.presentation.dto.room.response.Open
import smalltalk.backend.presentation.dto.room.response.SimpleInfo

interface RoomImplement {
    fun save(name: String): Open
    fun getById(id: Long): Room
    fun findAll(): List<SimpleInfo>
    fun addMember(id: Long): Enter
    fun deleteMember(sessionId: String, id: Long, memberId: Long): Room?
    fun sendSystemMessage(topic: String, type: SystemType, numberOfMember: Int, name: String, memberId: Long)
    fun sendChatMessage(topic: String, message: Any)
    fun sendErrorMessage(causedExceptionMessage: String?, sessionId: String, subscriptionId: String)
    fun saveMember(sessionId: String, memberId: Long, id: Long): Member
    fun findMember(sessionId: String): Member?
}