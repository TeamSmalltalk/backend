package smalltalk.backend.infrastructure.repository.room

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import smalltalk.backend.support.redis.RedisContainerConfig
import smalltalk.backend.support.redis.RedisTestConfig

@ActiveProfiles("test")
@SpringBootTest(classes = [RoomRepository::class, RedisRoomRepository::class, RedisTestConfig::class, RedisContainerConfig::class])
@DirtiesContext
internal class RoomRepositoryTest (
    private val roomRepository: RoomRepository
): ExpectSpec({
    val logger = KotlinLogging.logger {  }

    beforeEach {
        roomRepository.save("안녕하세요~")
        roomRepository.save("반가워요!")
        roomRepository.save("siuuuuu")
    }

    context("채팅방을 저장할 경우") {
        val roomName = "Team small talk"

        expect("첫 번째로 저장된 채팅방 id를 반환한다") {
            val savedRoomId = roomRepository.save(roomName)
            savedRoomId shouldBe 4L
        }
    }

    context("채팅방을 조회할 경우") {

        expect("첫 번째로 저장된 채팅방을 반환한다") {
            val firstFoundRoom = roomRepository.findById(1L)
            firstFoundRoom?.id shouldBe 1L
            firstFoundRoom?.name shouldBe "안녕하세요~"
        }

        expect("존재하지 않는 채팅방이면 null 값을 반환한다") {
            val foundRoom = roomRepository.findById(4L)
            foundRoom.shouldBeNull()
        }
    }

    afterEach {
        roomRepository.deleteAll()
    }
})