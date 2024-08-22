package smalltalk.backend.apply.infra.repository.room

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.FunSpec
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import smalltalk.backend.config.redis.RedisConfig
import smalltalk.backend.infra.repository.room.RedisRoomRepository
import smalltalk.backend.infra.repository.room.RoomRepository
import smalltalk.backend.support.redis.RedisContainerConfig

@ActiveProfiles("test")
@SpringBootTest(
    classes = [RoomRepository::class, RedisRoomRepository::class, RedisConfig::class, RedisContainerConfig::class]
)
@DirtiesContext
class ConcurrencyTest(
    private val roomRepository: RoomRepository
) : FunSpec({
    val logger = KotlinLogging.logger { }

    test("채팅방에 20명이 동시에 입장하면 정원이 10명이어야 한다") {
    }

    afterTest {
        roomRepository.deleteAll()
    }
})