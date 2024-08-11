package smalltalk.backend.apply.application.implement.room

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import smalltalk.backend.application.implement.room.RoomManager
import smalltalk.backend.apply.NAME
import smalltalk.backend.infrastructure.repository.room.RedisRoomRepository
import smalltalk.backend.infrastructure.repository.room.RoomRepository
import smalltalk.backend.support.redis.RedisContainerConfig
import smalltalk.backend.support.redis.RedisTestConfig
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@ActiveProfiles("test")
@SpringBootTest(
    classes = [RoomRepository::class, RedisRoomRepository::class, RedisTestConfig::class, RedisContainerConfig::class]
)
@DirtiesContext
class ConcurrencyTest(
    private val roomRepository: RoomRepository
) : FunSpec({
    val logger = KotlinLogging.logger { }
    val roomManager = RoomManager(roomRepository)

    test("채팅방에 20명이 동시에 입장하면 정원이 10명이어야 한다") {
        val savedRoomId = roomRepository.save(NAME).id
        val threadCount = 20
        val threadPool = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        repeat(threadCount) {
            threadPool.submit {
                try {
                    roomManager.addMember(savedRoomId)
                }
                finally {
                    latch.countDown()
                }
            }
        }
        latch.await()
        roomRepository.findById(savedRoomId)?.run {
            idQueue.shouldBeEmpty()
            members.size shouldBe 10
        }
    }

    afterTest {
        roomRepository.deleteAll()
    }
})