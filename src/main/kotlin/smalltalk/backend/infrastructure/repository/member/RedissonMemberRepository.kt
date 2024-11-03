package smalltalk.backend.infrastructure.repository.member

import io.github.oshai.kotlinlogging.KotlinLogging
import org.redisson.api.RedissonClient
import org.redisson.api.options.KeysScanParams
import org.springframework.stereotype.Repository
import smalltalk.backend.domain.member.Member

@Repository
class RedissonMemberRepository(private val client: RedissonClient) : MemberRepository {
    private val logger = KotlinLogging.logger { }
    companion object {
        private const val MEMBER_KEY_PREFIX = "room:member:"
        private const val MEMBER_KEY_PATTERN = "$MEMBER_KEY_PREFIX*"
    }

    override fun save(sessionId: String, id: Long, roomId: Long): Member {
        val memberToSave = Member(id, roomId)
        client.getBucket<Member>(MEMBER_KEY_PREFIX + sessionId).set(memberToSave)
        return memberToSave
    }

    override fun findById(sessionId: String): Member? {
        return client.getBucket<Member>(MEMBER_KEY_PREFIX + sessionId).get()
    }

    override fun findAll(): List<Member> =
        client.run {
            keys.getKeys(KeysScanParams().pattern(MEMBER_KEY_PATTERN)).mapNotNull { getBucket<Member>(it).get() }.toList()
        }

    override fun deleteById(sessionId: String) {
        client.keys.delete(MEMBER_KEY_PREFIX + sessionId)
    }

    override fun deleteAll() {
        client.keys.deleteByPattern(MEMBER_KEY_PATTERN)
    }
}