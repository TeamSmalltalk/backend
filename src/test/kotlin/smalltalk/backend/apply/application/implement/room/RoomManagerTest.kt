package smalltalk.backend.apply.application.implement.room

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import smalltalk.backend.application.exception.room.situation.RoomNotFoundException
import smalltalk.backend.application.implement.room.RoomManager
import smalltalk.backend.apply.NAME
import smalltalk.backend.apply.createRoom
import smalltalk.backend.infra.repository.room.RoomRepository
import smalltalk.backend.support.spec.afterRootTest

class RoomManagerTest : BehaviorSpec({
    val roomRepository = mockk<RoomRepository>()
    val roomManager = RoomManager(roomRepository)
    val logger = KotlinLogging.logger{ }

    Given("채팅방이 존재하는 경우") {
        val foundRooms = (1L..3L).map { createRoom(id = it) }
        val firstFoundRoom = foundRooms.first()
        roomRepository.run {
            every { findById(1L) } returns firstFoundRoom
            every { findAll() } returns foundRooms
            every { findById(4L) } returns null
        }
        When("id와 일치하는 채팅방을 조회하면") {
            val readRoom = roomManager.read(firstFoundRoom.id)
            Then("채팅방 정보가 반환된다") {
                readRoom.run {
                    name shouldBe NAME
                    idQueue shouldHaveSize 9
                    members shouldHaveSize 1
                }
            }
        }
        When("모든 채팅방을 조회하면") {
            val readRooms = roomManager.readAll()
            Then("채팅방 정보 리스트를 반환한다") {
                readRooms shouldHaveSize foundRooms.size
                readRooms.zip(foundRooms).forEach { (readRoom, foundRoom) ->
                    readRoom.run {
                        foundRoom.let {
                            id shouldBe it.id
                            name shouldBe it.name
                            memberCount shouldBe it.members.size
                        }
                    }
                }
            }
        }
        When("존재하지 않는 채팅방을 조회하면") {
            Then("예외가 발생한다") {
                shouldThrow<RoomNotFoundException> {
                    roomManager.read(4L)
                }
            }
        }
    }

    afterRootTest {
        clearAllMocks()
    }
})