package smalltalk.backend.infrastructure.repository.room

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.ints.shouldBeZero
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import smalltalk.backend.support.redis.RedisContainerConfig
import smalltalk.backend.support.redis.RedisTestConfig
import java.math.BigInteger
import java.math.BigInteger.*

@ActiveProfiles("test")
@SpringBootTest(classes = [RoomRepository::class, RedisRoomRepository::class, RedisTestConfig::class, RedisContainerConfig::class])
@DirtiesContext
internal class RoomRepositoryTest(
    private val roomRepository: RoomRepository
) : ExpectSpec({
    val logger = KotlinLogging.logger { }
    context("채팅방을 저장할 경우") {
        val roomName = "Team small talk 입니다~"
        expect("입력 받은 채팅방 이름을 통해 저장된 채팅방의 id를 반환한다") {
            roomRepository.save(roomName) shouldBe ONE
        }
    }

    context("채팅방을 id로 조회할 경우") {
        roomRepository.save("Team small talk 입니다~")
        expect("id가 1이면 일치하는 채팅방을 반환한다") {
            val foundRoom = roomRepository.findById(ONE)
            foundRoom?.run {
                id shouldBe ONE
                name shouldBe "Team small talk 입니다~"
                idQueue?.size shouldBe 10
                members?.size?.shouldBeZero()
            }
        }
        expect("id가 일치하는 채팅방이 없으면 null 값을 반환한다") {
            roomRepository.findById(ONE).shouldBeNull()
        }
    }

    context("모든 채팅방을 조회할 경우") {
        repeat(3) {
            roomRepository.save("채팅방$it")
        }
        expect("채팅방이 1개 이상 존재하면 모든 채팅방을 반환한다") {
            roomRepository.findAll().size shouldBe 3
        }
        expect("채팅방이 존재하지 않는다면 비어있는 리스트를 반환한다") {
            roomRepository.findAll().size.shouldBeZero()
        }
    }

    context("id가 일치하는 채팅방을 삭제할 경우") {
        roomRepository.save("Team small talk 입니다~")
        expect("id가 1이면 일치하는 채팅방을 삭제한다") {
            roomRepository.run {
                deleteById(ONE).shouldBeTrue()
                findById(ONE).shouldBeNull()
            }
        }
        expect("id가 일치하는 채팅방이 없으면 false 값을 반환한다") {
            roomRepository.deleteById(ONE).shouldBeFalse()
        }
    }

    context("모든 채팅방을 삭제할 경우") {
        repeat(3) {
            roomRepository.save("채팅방$it")
        }
        expect("채팅방이 1개 이상 존재하면 모든 채팅방을 삭제한다") {
            roomRepository.run {
                deleteAll()
                findAll().size.shouldBeZero()
            }
        }
    }

    context("채팅방에 입장할 경우") {
        val foundRoom =
            roomRepository.run {
                save("siuuuuu")?.let {
                    findById(it)
                }
            }
        expect("채팅방을 반환한다") {
            val updatedRoom =
                foundRoom?.let {
                    roomRepository.addMember(it)
                }
            updatedRoom?.run {
                idQueue?.size shouldBe 9
                members?.size shouldBe 1
                members?.last() shouldBe 1
            }
        }
    }

    afterEach {
        roomRepository.deleteAll()
    }
})