package smalltalk.backend.infra.repository.room

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import smalltalk.backend.apply.NAME
import smalltalk.backend.config.redis.RedisConfig
import smalltalk.backend.domain.room.Room
import smalltalk.backend.infra.repository.room.RedisRoomRepository
import smalltalk.backend.infra.repository.room.RoomRepository
import smalltalk.support.redis.RedisContainerConfig
import smalltalk.backend.util.jackson.ObjectMapperClient
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors


@ActiveProfiles("test")
@Import(RedisConfig::class, RedisContainerConfig::class, ObjectMapperClient::class)
@SpringBootTest(classes = [RoomRepository::class, RedisRoomRepository::class])
@DirtiesContext
class ConcurrencyTest(
    private val roomRepository: RoomRepository
) : FunSpec({
    val logger = KotlinLogging.logger { }

    test("채팅방에 9명의 멤버를 동시에 추가하면 정원이 10명이어야 한다") {
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

    test("가득찬 채팅방에서 동시에 모든 멤버를 삭제하면 채팅방이 삭제되어야 한다") {
        // Given
        var room: Room? = null
        val numberOfThread = 10
        val threadPool = Executors.newFixedThreadPool(numberOfThread)
        val latch = CountDownLatch(numberOfThread)
        val roomId = roomRepository.save(NAME).id
        repeat(9) {
            roomRepository.addMember(roomId)
        }

        // When
        (1L..10L).map {
            threadPool.submit {
                try {
                    room = roomRepository.deleteMember(roomId, it)
                }
                finally {
                    latch.countDown()
                }
            }
        }
        latch.await()

        // Then
        room.shouldBeNull()
    }

    afterTest {
        roomRepository.deleteAll()
    }
})