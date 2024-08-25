package smalltalk.backend.apply.infra.repository.room

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import smalltalk.backend.apply.NAME
import smalltalk.backend.config.redis.RedisConfig
import smalltalk.backend.infra.repository.room.RedisRoomRepository
import smalltalk.backend.infra.repository.room.RoomRepository
import smalltalk.backend.support.redis.RedisContainerConfig
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@ActiveProfiles("test")
@SpringBootTest(
    classes = [RoomRepository::class, RedisRoomRepository::class, RedisConfig::class, RedisContainerConfig::class]
)
@DirtiesContext
class ConcurrencyTest(
    private val roomRepository: RoomRepository
) : FunSpec({
    val logger = KotlinLogging.logger { }

    test("채팅방에 9명이 동시에 입장하면 정원이 10명이어야 한다") {
        // Given
        val numberOfThread = 9
        val threadPool = Executors.newFixedThreadPool(numberOfThread)
        val latch = CountDownLatch(numberOfThread)
        val roomId = roomRepository.save(NAME).id

        // When
        repeat(numberOfThread) {
            threadPool.submit {
                try {
                    roomRepository.addMember(roomId)
                }
                finally {
                    latch.countDown()
                }
            }
        }
        latch.await()

        // Then
        roomRepository.getById(roomId).run {
            idQueue shouldHaveSize 0
            members shouldHaveSize 10
        }
    }

    afterTest {
        roomRepository.deleteAll()
    }
})