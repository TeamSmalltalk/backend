package smalltalk.backend.infrastructure.repository.member

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.redis.core.StringRedisTemplate
import smalltalk.backend.domain.member.Member
import smalltalk.backend.infrastructure.repository.ObjectMapperClient

//@Repository
class LettuceMemberRepository(
    private val template: StringRedisTemplate,
    private val client: ObjectMapperClient
) : MemberRepository {
    private val logger = KotlinLogging.logger { }
    private val operations = template.opsForValue()
    companion object {
        private const val MEMBER_KEY_PREFIX = "room:member:"
        private const val MEMBER_KEY_PATTERN = "$MEMBER_KEY_PREFIX*"
    }

    override fun save(sessionId: String, id: Long, roomId: Long): Member {
        val memberToSave = Member(sessionId, id, roomId)
        operations[createKey(sessionId)] = client.getStringValue(memberToSave)
        return memberToSave
    }

    override fun findById(sessionId: String) = findByKey(createKey(sessionId))

    override fun findAll() = findKeys().mapNotNull { findByKey(it) }

    override fun deleteById(sessionId: String) {
        template.delete(createKey(sessionId))
    }

    override fun deleteAll() {
        template.run { delete(findKeys()) }
    }

    private fun createKey(sessionId: String) = MEMBER_KEY_PREFIX + sessionId

    private fun findByKey(key: String) =
        operations[key]?.let { client.getExpectedValue(it, Member::class.java) }

    private fun findKeys() = template.keys(MEMBER_KEY_PATTERN)
}