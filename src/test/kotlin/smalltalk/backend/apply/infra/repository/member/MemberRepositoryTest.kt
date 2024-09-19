package smalltalk.backend.apply.infra.repository.member

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import smalltalk.backend.apply.ID
import smalltalk.backend.apply.MEMBERS_INITIAL_ID
import smalltalk.backend.apply.MEMBER_SESSION_ID
import smalltalk.backend.config.redis.RedisConfig
import smalltalk.backend.exception.member.situation.MemberNotFoundException
import smalltalk.backend.infra.repository.member.MemberRepository
import smalltalk.backend.infra.repository.member.RedisMemberRepository
import smalltalk.backend.infra.repository.member.getById
import smalltalk.backend.support.redis.RedisContainerConfig
import smalltalk.backend.support.spec.afterRootTest
import smalltalk.backend.support.spec.beforeRootTest

@ActiveProfiles("test")
@Import(RedisConfig::class, RedisContainerConfig::class)
@SpringBootTest(classes = [MemberRepository::class, RedisMemberRepository::class])
@DirtiesContext
class MemberRepositoryTest(
    private val memberRepository: MemberRepository
) : ExpectSpec({
    val logger = KotlinLogging.logger { }
    val sessionId = MEMBER_SESSION_ID
    val id = MEMBERS_INITIAL_ID
    val roomId = ID

    beforeRootTest {
        memberRepository.save(sessionId, id, roomId)
    }

    context("채팅방 멤버 조회") {
        expect("id와 일치하는 멤버를 반환한다") {
            val member = memberRepository.getById(sessionId)
            member.let {
                it.id shouldBe id
                it.roomId shouldBe roomId
            }
        }
        expect("예외가 발생한다") {
            shouldThrow<MemberNotFoundException> {
                memberRepository.getById("non-existent-id")
            }
        }
    }

    expect("id와 일치하는 멤버를 삭제한다") {
        memberRepository.run {
            deleteById(sessionId)
            findById(sessionId).shouldBeNull()
        }
    }

    afterRootTest {
        memberRepository.deleteAll()
    }
})