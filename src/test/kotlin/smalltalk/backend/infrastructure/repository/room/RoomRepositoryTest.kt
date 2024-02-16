package smalltalk.backend.infrastructure.repository.room

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import smalltalk.backend.config.redis.RedisContainerConfig
import smalltalk.backend.config.redis.TestRedisConfig

@ActiveProfiles("test")
@SpringBootTest(classes = [RoomRepository::class, RedisRoomRepository::class, TestRedisConfig::class, RedisContainerConfig::class])
@DirtiesContext
internal class RoomRepositoryTest (
    private val roomRepository: RoomRepository
): DescribeSpec({

    val logger = KotlinLogging.logger {  }

    afterEach {
        roomRepository.deleteAll()
        logger.info { "Delete all room" }
    }

    describe("채팅방을 생성할 때") {

        context("채팅방 이름을 입력받으면") {

            val roomId = roomRepository.save("My 채팅방")

            it("채팅방 id를 반환한다") {

                val room = roomRepository.findById(roomId)
                room?.id shouldBe roomId
            }
        }
    }
})