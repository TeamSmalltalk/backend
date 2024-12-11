package smalltalk.backend.infrastructure.repository.member

import io.github.oshai.kotlinlogging.KotlinLogging
import org.redisson.api.RedissonClient
import org.redisson.api.options.KeysScanParams
import org.springframework.stereotype.Repository
import smalltalk.backend.domain.member.Member
import smalltalk.backend.infrastructure.repository.ObjectMapperClient

@Repository
class RedissonMemberRepository(
    private val redisson: RedissonClient,
    private val objectMapper: ObjectMapperClient
) : MemberRepository {
    companion object {
        private const val KEY_PREFIX = "room:member:"
        private const val KEY_PATTERN = "$KEY_PREFIX*"
    }
    private val logger = KotlinLogging.logger { }

    override fun save(sessionId: String, id: Long, roomId: Long) =
        Member(sessionId, id, roomId).also { redisson.getBucket<String>(KEY_PREFIX + sessionId).set(objectMapper.getStringValue(it)) }

    override fun findById(sessionId: String) = findByKey(KEY_PREFIX + sessionId)

    override fun findAll() = redisson.run { keys.getKeys(KeysScanParams().pattern(KEY_PATTERN)).mapNotNull { findByKey(it) }.toList() }

    override fun deleteById(sessionId: String) {
        redisson.keys.delete(KEY_PREFIX + sessionId)
    }

    override fun deleteAll() {
        redisson.keys.flushdb()
    }

    private fun findByKey(key: String) = redisson.getBucket<String>(key).get()?.let { getExpectedValue<Member>(it) }

    private inline fun <reified T : Any> getExpectedValue(value: Any) = objectMapper.getExpectedValue(value, T::class.java)
}