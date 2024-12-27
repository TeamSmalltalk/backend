package smalltalk.backend.domain.member

import smalltalk.backend.exception.room.situation.MemberNotFoundException

fun MemberRepository.getById(sessionId: String) = findById(sessionId) ?: throw MemberNotFoundException()

interface MemberRepository {
    fun save(sessionId: String, id: Long, roomId: Long): Member
    fun findById(sessionId: String): Member?
    fun findAll(): List<Member>
    fun deleteById(sessionId: String)
    fun deleteAll()
}