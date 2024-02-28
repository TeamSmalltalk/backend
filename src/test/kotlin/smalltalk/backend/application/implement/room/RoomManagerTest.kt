package smalltalk.backend.application.implement.room

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import smalltalk.backend.application.exception.room.situation.RoomNotFoundException
import smalltalk.backend.domain.room.Room
import smalltalk.backend.infrastructure.repository.room.RoomRepository
import smalltalk.backend.support.spec.afterRootTest

class RoomManagerTest : BehaviorSpec({
    val roomRepository = mockk<RoomRepository>()
    val roomManager = RoomManager(roomRepository)
    val logger = KotlinLogging.logger{ }

    Given("id가 1L인 채팅방만 존재하는 경우") {
        val room =
            Room(
                1L,
                "안녕하세요~",
                (2L..10L).toMutableList(),
                mutableListOf(1L)
            )
        every { roomRepository.findById(1L) } returns room
        every { roomRepository.findById(2L) } returns null
        When("id가 1L인 채팅방을 조회하면") {
            val readRoom = roomManager.read(room.id)
            Then("id와 일치하는 채팅방이 반환된다") {
                readRoom.run {
                    id shouldBe 1L
                    name shouldBe "안녕하세요~"
                    idQueue.size shouldBe 9
                    members.size shouldBe 1
                    members.last() shouldBe 1L
                }
            }
        }
        When("id가 2L인 채팅방을 조회하면") {
            Then("예외가 발생한다") {
                shouldThrow<RoomNotFoundException> {
                    roomManager.read(2L)
                }
            }
        }
    }

    afterRootTest {
        clearAllMocks()
    }
})