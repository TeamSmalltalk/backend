package smalltalk.backend.apply.service.room

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
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

    Given("채팅방이 여러개 존재하는 경우") {
        val rooms = (1L..3L).map { create(it, "room$it") }
        every { roomRepository.findAll() } returns rooms
        When("채팅방 목록을 조회하면") {
            val simpleInfos = roomService.getSimpleInfos()
            Then("모든 채팅방을 반환한다") {
                simpleInfos shouldHaveSize 3
            }
        }
    }

    Given("입장할 채팅방에 대한 정보가 있는 경우") {
        val room = create()
        val enteredMemberId = room.members.last()
        every { roomRepository.addMember(any()) } returns enteredMemberId
        When("채팅방에 입장하면") {
            val response = roomService.enter(room.id)
            Then("채팅방 멤버 정보를 반환한다") {
                response.memberId shouldBe enteredMemberId
            }
        }
    }
})