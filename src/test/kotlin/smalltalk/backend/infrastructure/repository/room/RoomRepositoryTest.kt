package smalltalk.backend.infrastructure.repository.room

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.ExpectSpec
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

    afterContainer {

        roomRepository.deleteAll()
        logger.info { "Delete all room" }
    }

    context("채팅방을 2개 저장할 경우") {

        val firstRoomName = "안녕하세요~"
        val secondRoomName = "반가워요!"

        expect("첫 번째 채팅방 id를 반환한다.") {

            val firstSavedRoomId = roomRepository.save(firstRoomName)

            firstSavedRoomId shouldBe 1L
        }

        expect("두 번째 채팅방 id를 반환한다.") {

            val secondSavedRoomId = roomRepository.save(secondRoomName)

            secondSavedRoomId shouldBe 2L
        }
    }
})