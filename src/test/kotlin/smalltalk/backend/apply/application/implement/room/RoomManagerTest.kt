package smalltalk.backend.apply.application.implement.room

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import smalltalk.backend.application.exception.room.situation.RoomNotFoundException
import smalltalk.backend.application.implement.room.RoomManager
import smalltalk.backend.apply.ID
import smalltalk.backend.apply.MEMBERS_INITIAL_ID
import smalltalk.backend.apply.NAME
import smalltalk.backend.apply.createRoom
import smalltalk.backend.infra.repository.room.RoomRepository
import smalltalk.backend.support.spec.afterRootTest

class RoomManagerTest : BehaviorSpec({
    val roomRepository = mockk<RoomRepository>()
    val roomManager = RoomManager(roomRepository)
    val logger = KotlinLogging.logger{ }

    Given("채팅방 이름이 있는 경우") {
        val roomName = "안녕하세요~"
        val savedRoom = createRoom(name = roomName)
        every { roomRepository.save(any()) } returns savedRoom
        When("채팅방을 저장하면") {
            val openResponse = roomManager.create(roomName)
            Then("room id와 member id가 반환된다") {
                openResponse.run {
                    roomId shouldBe savedRoom.id
                    memberId shouldBe savedRoom.members.last()
                }
            }
        }
    }

    Given("id가 1L인 채팅방만 존재하는 경우") {
        val foundRoom = createRoom()
        every { roomRepository.findById(1L) } returns foundRoom
        every { roomRepository.findById(2L) } returns null
        When("id가 1L인 채팅방을 조회하면") {
            val readRoom = roomManager.read(foundRoom.id)
            Then("id와 일치하는 채팅방이 반환된다") {
                readRoom.run {
                    id shouldBe ID
                    name shouldBe NAME
                    idQueue.size shouldBe 9
                    members.size shouldBe 1
                    members.last() shouldBe MEMBERS_INITIAL_ID
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

    Given("채팅방이 여러 개 존재하는 경우") {
        val foundRooms = (1L..3L).map {
            createRoom(id = it)
        }
        every { roomRepository.findAll() } returns foundRooms
        When("모든 채팅방을 조회하면") {
            val readRooms = roomManager.readAll()
            Then("채팅방 id, 이름, 멤버 수로 이루어진 리스트를 반환한다") {
                readRooms.size shouldBe foundRooms.size
                readRooms.zip(foundRooms).forEach { (readRoom, foundRoom) ->
                    readRoom.id shouldBe foundRoom.id
                    readRoom.name shouldBe foundRoom.name
                    readRoom.memberCount shouldBe foundRoom.members.size
                }
            }
        }
    }

    afterRootTest {
        clearAllMocks()
    }
})