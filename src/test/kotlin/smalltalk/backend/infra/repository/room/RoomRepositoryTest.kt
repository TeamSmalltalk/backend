package smalltalk.backend.infra.repository.room

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.collections.*
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import smalltalk.backend.ID
import smalltalk.backend.NAME
import smalltalk.backend.config.redis.RedisConfig
import smalltalk.backend.exception.room.situation.FullRoomException
import smalltalk.backend.exception.room.situation.RoomNotFoundException
import smalltalk.backend.util.jackson.ObjectMapperClient
import smalltalk.support.EnableTestContainers
import smalltalk.support.spec.afterRootTest

@SpringBootTest(classes = [RedisConfig::class, RoomRepository::class, RedisRoomRepository::class, ObjectMapperClient::class])
@EnableTestContainers
class RoomRepositoryTest(private val roomRepository: RoomRepository) : ExpectSpec({
    val logger = KotlinLogging.logger { }

    expect("채팅방을 저장한다") {
        val savedRoom = roomRepository.save(NAME)
        savedRoom.run {
            id shouldBe ID
            name shouldBe NAME
            idQueue shouldHaveSize 9
            members shouldHaveSize 1
        }
    }

    context("채팅방 조회") {
        (1..3).map { roomRepository.save(NAME + it) }
        expect("id와 일치하는 채팅방을 조회한다") {
            val room = roomRepository.getById(ID)
            room.run {
                name shouldBe (NAME + 1)
                idQueue shouldHaveSize 9
                members shouldHaveSize 1
            }
        }
        expect("예외가 발생한다") {
            shouldThrow<RoomNotFoundException> {
                roomRepository.getById(4L)
            }
        }
        expect("모든 채팅방을 조회한다") {
            roomRepository.findAll() shouldHaveSize 3
        }
    }

    context("채팅방 삭제") {
        (1..3).map { roomRepository.save(NAME + it) }
        expect("모든 채팅방을 삭제한다") {
            roomRepository.deleteAll()
            roomRepository.findAll().shouldBeEmpty()
        }
    }

    context("채팅방 멤버 추가") {
        val roomId = roomRepository.save(NAME).id
        expect("추가된 멤버의 id를 반환한다") {
            val memberIds =
                (2..10).map {
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
        val memberIdToDelete = roomRepository.addMember(roomId)
        expect("2명 이상 존재하면 멤버를 삭제한다") {
            val room = roomRepository.deleteMember(roomId, memberIdToDelete)
            room?.run {
                idQueue shouldContain memberIdToDelete
                members shouldNotContain memberIdToDelete
            }
        }
        expect("1명만 존재하면 채팅방을 삭제한다") {
            val room = roomRepository.deleteMember(roomId, 1L)
            room.shouldBeNull()
        }
    }

    afterRootTest {
        roomRepository.deleteAll()
    }
})