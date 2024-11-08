package smalltalk.backend.apply.infrastructure.repository.room

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import smalltalk.backend.apply.*
import smalltalk.backend.config.redis.RedisConfig
import smalltalk.backend.infrastructure.repository.room.RedissonRoomRepository
import smalltalk.backend.infrastructure.repository.room.RoomRepository
import smalltalk.backend.infrastructure.repository.room.getById
import smalltalk.backend.util.jackson.ObjectMapperClient
import smalltalk.backend.support.EnableTestContainers
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

@SpringBootTest(classes = [RedisConfig::class, RedissonRoomRepository::class, ObjectMapperClient::class])
@EnableTestContainers
class RoomRepositoryConcurrencyTest(private val roomRepository: RoomRepository) : FunSpec({
    val logger = KotlinLogging.logger { }

    test("채팅방에 9명의 멤버를 동시에 추가하면 정원이 10명이어야 한다") {
        // Given
        val numberOfThread = (PROVIDER_LIMIT - 1).toInt()
        val threadPool = Executors.newFixedThreadPool(numberOfThread)
        val latch = CountDownLatch(numberOfThread)
        val id = roomRepository.save(NAME).id

        // When
        repeat(numberOfThread) {
            threadPool.submit {
                try {
                    roomRepository.addMember(id)
                }
                finally {
                    latch.countDown()
                }
            }
        }
        latch.await()

        // Then
        roomRepository.getById(id).numberOfMember shouldBe 10
    }

    test("가득찬 채팅방에서 동시에 모든 멤버를 삭제하면 채팅방이 삭제되어야 한다") {
        // Given
        val numberOfThread = PROVIDER_LIMIT.toInt()
        val threadPool = Executors.newFixedThreadPool(numberOfThread)
        val latch = CountDownLatch(numberOfThread)
        val id = roomRepository.save(NAME).id
        repeat(9) {
            roomRepository.addMember(id)
        }

        // When
        repeat(PROVIDER_LIMIT.toInt()) {
            threadPool.submit {
                try {
                    roomRepository.deleteMember(id, it.toLong())
                }
                finally {
                    latch.countDown()
                }
            }
        }
        latch.await()

        // Then
        roomRepository.findById(id).shouldBeNull()
    }

    afterTest {
        roomRepository.deleteAll()
    }
})