package smalltalk.backend.apply.infrastructure.repository.room

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.collections.*
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import smalltalk.backend.apply.*
import smalltalk.backend.config.redis.RedisConfig
import smalltalk.backend.exception.room.situation.RoomNotFoundException
import smalltalk.backend.infrastructure.repository.room.RedissonRoomRepository
import smalltalk.backend.infrastructure.repository.room.RoomRepository
import smalltalk.backend.infrastructure.repository.room.getById
import smalltalk.backend.support.EnableTestContainers
import smalltalk.backend.support.spec.afterRootTest
import smalltalk.backend.util.jackson.ObjectMapperClient

@SpringBootTest(classes = [RedisConfig::class, RoomRepository::class, RedissonRoomRepository::class, ObjectMapperClient::class])
@EnableTestContainers
class RoomRepositoryTest(private val roomRepository: RoomRepository) : ExpectSpec({
    val logger = KotlinLogging.logger { }

    expect("채팅방을 저장한다") {
        roomRepository.save(NAME).run {
            id shouldBe ID
            name shouldBe NAME
            numberOfMember shouldBe 1
        }
    }

    context("채팅방 조회") {
        (1..3).map { roomRepository.save(NAME + it) }
        expect("id와 일치하는 채팅방을 조회한다") {
            roomRepository.getById(ID).run {
                id shouldBe ID
                name shouldBe (NAME + 1)
                numberOfMember shouldBe 1
            }
        }
        expect("예외가 발생한다") {
            roomRepository.findById(4L).shouldBeNull()
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
            roomRepository.run {
                deleteAll()
                findAll().shouldBeEmpty()
            }
        }
    }

//    context("채팅방 멤버 추가") {
//        val roomId = roomRepository.save(NAME).id
//        expect("추가된 멤버의 id를 반환한다") {
//            repeat((PROVIDER_LIMIT - MEMBER_INIT).toInt()) {
//                roomRepository.addMember(roomId)
//            }
//            roomRepository.getById(roomId).numberOfMember shouldBe PROVIDER_LIMIT
//        }
//        expect("예외가 발생한다") {
//            shouldThrow<FullRoomException> {
//                roomRepository.addMember(roomId)
//            }
//        }
//    }
//
//    context("채팅방 멤버 삭제") {
//        val roomId = roomRepository.save(NAME).id
//        val memberIdToDelete = roomRepository.addMember(roomId)
//        expect("2명 이상 존재하면 멤버를 삭제한다") {
//            roomRepository.deleteMember(roomId, memberIdToDelete)?.numberOfMember shouldBe MEMBER_INIT
//        }
//        expect("1명만 존재하면 채팅방을 삭제한다") {
//            logger.info { roomRepository.deleteMember(roomId, MEMBER_INIT)?.numberOfMember }
//        }
//    }

    afterRootTest {
        roomRepository.deleteAll()
    }
})