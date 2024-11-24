package smalltalk.backend.apply.infrastructure.repository.room

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.collections.*
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import smalltalk.backend.apply.*
import smalltalk.backend.config.redis.RedisConfig
import smalltalk.backend.exception.room.situation.FullRoomException
import smalltalk.backend.exception.room.situation.RoomNotFoundException
import smalltalk.backend.infrastructure.repository.room.*
import smalltalk.backend.support.EnableTestContainers
import smalltalk.backend.support.spec.afterRootTest
import smalltalk.backend.util.jackson.ObjectMapperClient

@SpringBootTest(
    classes = [RedisConfig::class, RoomRedisFunctionsLoader::class, RedissonRoomRepository::class, ObjectMapperClient::class]
)
@EnableConfigurationProperties(value = [RoomYamlProperties::class])
@EnableTestContainers
class RoomRepositoryTest(private val roomRepository: RoomRepository) : ExpectSpec({
    val logger = KotlinLogging.logger { }

    expect("채팅방을 저장한다") {
        roomRepository.save(NAME).run {
            id shouldBe ID
            name shouldBe NAME
            numberOfMember shouldBe MEMBER_INIT
        }
    }

    context("채팅방 조회") {
        repeat(3) { roomRepository.save(NAME + it) }
        expect("id와 일치하는 채팅방을 조회한다") {
            roomRepository.findById(ID)?.run {
                id shouldBe ID
                name shouldBe (NAME + 0)
                numberOfMember shouldBe MEMBER_INIT
            }
        }
        expect("모든 채팅방을 조회한다") {
            roomRepository.findAll() shouldHaveSize 3
        }
    }

    context("채팅방 삭제") {
        repeat(3) { roomRepository.save(NAME + it) }
        expect("모든 채팅방을 삭제한다") {
            roomRepository.run {
                deleteAll()
                findAll().shouldBeEmpty()
            }
        }
    }

    context("채팅방 멤버 추가") {
        val id = roomRepository.save(NAME).id
        expect("추가된 멤버의 id를 반환한다") {
            roomRepository.run {
                repeat(MEMBER_LIMIT - MEMBER_INIT) { addMember(id) }
                getById(id).numberOfMember shouldBe MEMBER_LIMIT
            }
        }
        expect("존재하지 않는 예외가 발생한다") {
            shouldThrow<RoomNotFoundException> { roomRepository.addMember(2L) }
        }
        expect("가득찬 예외가 발생한다") {
            shouldThrow<FullRoomException> { roomRepository.addMember(id) }
        }
    }

    context("채팅방 멤버 삭제") {
        val id = roomRepository.save(NAME).id
        val memberIdToDelete = roomRepository.addMember(id)
        expect("2명 이상 존재하면 멤버를 삭제한다") {
            roomRepository.deleteMember(id, memberIdToDelete)?.numberOfMember shouldBe MEMBER_INIT
        }
        expect("1명만 존재하면 채팅방을 삭제한다") {
            roomRepository.run {
                deleteMember(id, MEMBER_INIT.toLong())
                findById(id).shouldBeNull()
            }
        }
        expect("예외가 발생한다") {
            shouldThrow<RoomNotFoundException> { roomRepository.deleteMember(id, MEMBER_INIT.toLong()) }
        }
    }

    afterRootTest {
        roomRepository.deleteAll()
    }
})