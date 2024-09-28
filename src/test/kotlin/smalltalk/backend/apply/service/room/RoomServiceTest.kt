package smalltalk.backend.apply.service.room

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import smalltalk.backend.application.service.room.RoomService
import smalltalk.backend.apply.create
import smalltalk.backend.apply.createOpenRequest
import smalltalk.backend.infra.repository.room.RoomRepository

class RoomServiceTest : BehaviorSpec({
    val roomRepository = mockk<RoomRepository>()
    val roomService = RoomService(roomRepository)
    val logger = KotlinLogging.logger { }

    Given("생성할 채팅방에 대한 정보가 있는 경우") {
        val room = create()
        val request = createOpenRequest()
        every { roomRepository.save(any()) } returns room
        When("채팅방을 생성하면") {
            val response = roomService.open(request)
            Then("생성된 채팅방에 대한 정보를 반환한다") {
                response.run {
                    id shouldBe room.id
                    memberId shouldBe room.members.last()
                }
            }
        }
    }
})