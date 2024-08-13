package smalltalk.backend.apply.infrastructure.repository.room

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import smalltalk.backend.apply.NAME
import smalltalk.backend.infrastructure.repository.room.RedisRoomRepository
import smalltalk.backend.infrastructure.repository.room.RoomRepository
import smalltalk.backend.support.redis.RedisContainerConfig
import smalltalk.backend.support.redis.RedisTestConfig
import smalltalk.backend.support.spec.afterRootTest

@ActiveProfiles("test")
@SpringBootTest(
    classes = [RoomRepository::class, RedisRoomRepository::class, RedisTestConfig::class, RedisContainerConfig::class]
)
@DirtiesContext
class RoomRepositoryTest(
    private val roomRepository: RoomRepository
) : ExpectSpec({
    val logger = KotlinLogging.logger { }

    context("채팅방 저장") {
        val roomName = NAME
        expect("저장된 채팅방을 반환한다") {
            val savedRoom = roomRepository.save(roomName)
            savedRoom.run {
                id shouldBe 1L
                name shouldBe NAME
                idQueue shouldHaveSize 9
                members shouldHaveSize 1
            }
        }
    }

    context("채팅방 조회") {
        repeat(3) {
            roomRepository.save("채팅방$it")
        }
        expect("id와 일치하는 채팅방을 조회한다") {
            val foundRoom = roomRepository.findById(1L)
            foundRoom?.run {
                name shouldBe "채팅방0"
                idQueue shouldHaveSize 9
                members shouldHaveSize 1
            }
        }
        expect("모든 채팅방을 조회한다") {
            roomRepository.findAll() shouldHaveSize 3
        }
    }

    context("채팅방 멤버 추가") {
        val savedRoom = roomRepository.save(NAME)
        expect("추가된 memberId를 반환한다") {
            val memberId = roomRepository.addMember(savedRoom)
            roomRepository.findById(savedRoom.id)?.run {
                idQueue.shouldNotContain(memberId)
                members.shouldContain(memberId)
            }
        }
    }

    context("채팅방 멤버 삭제") {
        val savedRoom = roomRepository.save(NAME)
        val memberIdToDelete = 1L
        expect("일치하는 memberId를 삭제한다") {
            roomRepository.run {
                deleteMember(savedRoom, memberIdToDelete)
                findById(savedRoom.id)?.run {
                    idQueue.shouldContain(memberIdToDelete)
                    members.shouldNotContain(memberIdToDelete)
                }
            }
        }
    }

    context("채팅방 삭제") {
        val savedRooms =
            (0 until 3).map {
                roomRepository.save("채팅방$it")
            }.toList()
        expect("정보와 일치하는 채팅방을 삭제한다") {
            roomRepository.run {
                deleteByRoom(savedRooms.last())
                findById(3L).shouldBeNull()
            }
        }
        expect("모든 채팅방을 삭제한다") {
            roomRepository.run {
                deleteAll()
                findAll().shouldBeEmpty()
            }
        }
    }

    afterRootTest {
        roomRepository.deleteAll()
    }
})