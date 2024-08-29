package smalltalk.backend.apply.infra.repository.room

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.collections.*
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import smalltalk.backend.apply.NAME
import smalltalk.backend.config.redis.RedisConfig
import smalltalk.backend.infra.exception.room.situation.FullRoomException
import smalltalk.backend.infra.exception.room.situation.RoomNotFoundException
import smalltalk.backend.infra.repository.room.RedisRoomRepository
import smalltalk.backend.infra.repository.room.RoomRepository
import smalltalk.backend.support.redis.RedisContainerConfig
import smalltalk.backend.support.spec.afterRootTest

@ActiveProfiles("test")
@SpringBootTest(
    classes = [RoomRepository::class, RedisRoomRepository::class, RedisConfig::class, RedisContainerConfig::class]
)
@DirtiesContext
class RoomRepositoryTest(
    private val roomRepository: RoomRepository
) : ExpectSpec({
    context("채팅방 저장") {
        val roomName = NAME
        expect("채팅방을 반환한다") {
            val savedRoom = roomRepository.save(roomName)
            savedRoom.run {
                id shouldBe 1L
                name shouldBe NAME
                idQueue shouldHaveSize 10
                members shouldHaveSize 0
            }
        }
    }

    context("채팅방 조회") {
        (1..3).map { roomRepository.save("채팅방$it") }
        expect("id와 일치하는 채팅방을 조회한다") {
            val room = roomRepository.getById(1L)
            room.run {
                name shouldBe "채팅방1"
                idQueue shouldHaveSize 10
                members shouldHaveSize 0
            }
        }
        expect("모든 채팅방을 조회한다") {
            roomRepository.findAll() shouldHaveSize 3
        }
    }

    context("채팅방 멤버 추가") {
        val roomId = roomRepository.save(NAME).id
        expect("추가된 멤버의 id를 반환한다") {
            val memberIds =
                (1..10).map {
                    roomRepository.addMember(roomId)
                }.toList()
            roomRepository.getById(roomId).run {
                idQueue shouldNotContainAll memberIds
                members shouldContainAll memberIds
            }
        }
        expect("예외가 발생한다") {
            shouldThrow<FullRoomException> {
                roomRepository.addMember(roomId)
            }
        }
    }

    context("채팅방 멤버 삭제") {
        val roomId = roomRepository.save(NAME).id
        (1..2).map { roomRepository.addMember(roomId) }
        expect("2명 이상 존재하면 멤버를 삭제한다") {
            roomRepository.deleteMember(roomId, 2L)
            roomRepository.getById(roomId).run {
                idQueue shouldContain 2L
                members shouldNotContain 2L
            }
        }
        expect("1명만 존재하면 채팅방을 삭제한다") {
            roomRepository.deleteMember(roomId, 1L)
            shouldThrow<RoomNotFoundException> {
                roomRepository.getById(roomId)
            }
        }
    }

    afterRootTest {
        roomRepository.deleteAll()
    }
})