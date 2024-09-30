package smalltalk.backend.infra.repository.member

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
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
import smalltalk.backend.exception.room.situation.MemberNotFoundException
import smalltalk.backend.infra.repository.member.MemberRepository
import smalltalk.backend.infra.repository.member.RedisMemberRepository
import smalltalk.backend.infra.repository.member.getById
import smalltalk.support.redis.RedisContainerConfig
import smalltalk.support.spec.afterRootTest
import smalltalk.backend.util.jackson.ObjectMapperClient

@ActiveProfiles("test")
@Import(RedisConfig::class, RedisContainerConfig::class, ObjectMapperClient::class)
@SpringBootTest(classes = [MemberRepository::class, RedisMemberRepository::class])
@DirtiesContext
class MemberRepositoryTest(
    private val memberRepository: MemberRepository
) : ExpectSpec({
    val logger = KotlinLogging.logger { }
    val sessionId = MEMBER_SESSION_ID
    val id = MEMBERS_INITIAL_ID
    val roomId = ID

    expect("채팅방 멤버를 저장한다") {
        val savedMember = memberRepository.save(sessionId, id, roomId)
        memberRepository.getById(sessionId).run {
            savedMember.id shouldBe id
            savedMember.roomId shouldBe roomId
        }
    }

    context("채팅방 멤버 조회") {
        (1L..3L).map { memberRepository.save("session-id$it", it, roomId) }
        expect("id와 일치하는 멤버를 반환한다") {
            val member = memberRepository.getById(sessionId + 1L)
            member.id shouldBe 1L
            member.roomId shouldBe roomId
        }
        expect("예외가 발생한다") {
            shouldThrow<MemberNotFoundException> {
                memberRepository.getById("non-existent-session-id")
            }
        }
        expect("모든 멤버를 조회한다") {
            memberRepository.findAll() shouldHaveSize 3
        }
    }

    context("채팅방 멤버 삭제") {
        (1L..3L).map { memberRepository.save("session-id$it", it, roomId) }
        expect("id와 일치하는 멤버를 삭제한다") {
            val idToDelete = sessionId + 3L
            memberRepository.run {
                deleteById(idToDelete)
                findById(idToDelete).shouldBeNull()
            }
        }
        expect("모든 멤버를 삭제한다") {
            memberRepository.run {
                deleteAll()
                findAll().shouldBeEmpty()
            }
        }
    }

    afterRootTest {
        memberRepository.deleteAll()
    }
})