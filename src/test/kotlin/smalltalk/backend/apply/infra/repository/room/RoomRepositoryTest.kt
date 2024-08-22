package smalltalk.backend.apply.infra.repository.room

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import smalltalk.backend.apply.NAME
import smalltalk.backend.config.redis.RedisConfig
import smalltalk.backend.infra.repository.room.RedisRoomRepository
import smalltalk.backend.infra.repository.room.RoomRepository
import smalltalk.backend.support.redis.RedisContainerConfig
import smalltalk.backend.support.spec.afterRootTest

@ActiveProfiles("test")
@SpringBootTest(
    classes = [RoomRepository::class, RedisRoomRepository::class, RedisConfig::class, RedisContainerConfig::class]
)
@DirtiesContext
class RoomRepositoryTest(
    private val roomRepository: RoomRepository
) : ExpectSpec({
    val logger = KotlinLogging.logger { }

    context("채팅방 저장") {
        val roomName = NAME
        expect("채팅방을 반환한다") {
            val savedRoom = roomRepository.save(roomName)
            savedRoom.run {
                id shouldBe 1L
                name shouldBe NAME
                idQueue shouldHaveSize 9
                members shouldHaveSize 1
            }
        }
    }

    context("채팅방 조회") {
        (1..3).map { roomRepository.save("채팅방$it") }
        expect("id와 일치하는 채팅방을 조회한다") {
            val room = roomRepository.getById(1L)
            room.run {
                name shouldBe "채팅방1"
                idQueue shouldHaveSize 9
                members shouldHaveSize 1
            }
        }
        expect("모든 채팅방을 조회한다") {
            roomRepository.findAll() shouldHaveSize 3
        }
    }

    context("채팅방 멤버 추가") {
    }

    context("채팅방 멤버 삭제") {
    }

    afterRootTest {
        roomRepository.deleteAll()
    }
})