package smalltalk.backend.apply.infrastructure.repository.member

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import smalltalk.backend.apply.ID
import smalltalk.backend.apply.MEMBER_INIT
import smalltalk.backend.apply.MEMBER_SESSION_ID
import smalltalk.backend.config.redis.RedisConfig
import smalltalk.backend.infrastructure.repository.member.MemberRepository
import smalltalk.backend.infrastructure.repository.member.RedissonMemberRepository
import smalltalk.backend.infrastructure.repository.member.getById
import smalltalk.backend.support.EnableTestContainers
import smalltalk.backend.support.spec.afterRootTest
import smalltalk.backend.infrastructure.repository.ObjectMapperClient

@SpringBootTest(classes = [RedisConfig::class, RedissonMemberRepository::class, ObjectMapperClient::class])
@EnableTestContainers
class MemberRepositoryTest(private val memberRepository: MemberRepository) : ExpectSpec({
    val logger = KotlinLogging.logger { }

    expect("채팅방 멤버를 저장한다") {
        val savedMember = memberRepository.save(MEMBER_SESSION_ID, MEMBER_INIT.toLong(), ID)
        memberRepository.getById(MEMBER_SESSION_ID).run {
            savedMember.sessionId shouldBe sessionId
            savedMember.id shouldBe id
            savedMember.roomId shouldBe roomId
        }
    }

    context("채팅방 멤버 조회") {
        repeat(3) { memberRepository.save(MEMBER_SESSION_ID + (it + 1), (it + 1).toLong(), ID) }
        expect("id와 일치하는 멤버를 반환한다") {
            memberRepository.findById(MEMBER_SESSION_ID + MEMBER_INIT)?.run {
                sessionId shouldBe (MEMBER_SESSION_ID + MEMBER_INIT)
                id shouldBe MEMBER_INIT
                roomId shouldBe ID
            }
        }
        expect("모든 멤버를 조회한다") {
            memberRepository.findAll() shouldHaveSize 3
        }
    }

    context("채팅방 멤버 삭제") {
        repeat(3) { memberRepository.save(MEMBER_SESSION_ID + it + 1, (it + 1).toLong(), ID) }
        expect("id와 일치하는 멤버를 삭제한다") {
            val idToDelete = MEMBER_SESSION_ID + MEMBER_INIT
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