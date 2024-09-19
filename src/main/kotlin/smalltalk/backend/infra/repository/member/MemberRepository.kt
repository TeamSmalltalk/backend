package smalltalk.backend.infra.repository.member

import smalltalk.backend.domain.member.Member

interface MemberRepository {
    fun save(sessionId: String): Member
    fun findById(sessionId: String): Member?
    fun deleteById(sessionId: String)
}