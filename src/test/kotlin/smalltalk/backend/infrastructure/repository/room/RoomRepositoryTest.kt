package smalltalk.backend.infrastructure.repository.room

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.ints.shouldBeZero
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import smalltalk.backend.domain.room.Room
import smalltalk.backend.support.redis.RedisContainerConfig
import smalltalk.backend.support.redis.RedisTestConfig
import java.math.BigInteger.*

@ActiveProfiles("test")
@SpringBootTest(classes = [RoomRepository::class, RedisRoomRepository::class, RedisTestConfig::class, RedisContainerConfig::class])
@DirtiesContext
internal class RoomRepositoryTest(
    private val roomRepository: RoomRepository
) : ExpectSpec({
    val logger = KotlinLogging.logger { }

    context("채팅방을 저장할 경우") {
        val roomName = "Team small talk 입니다~"
        expect("입력 받은 채팅방 이름을 통해 저장된 채팅방의 id를 반환한다") {
            roomRepository.save(roomName) shouldBe ONE
        }
    }

    context("채팅방을 id로 조회할 경우") {
        roomRepository.save("Team small talk 입니다~")
        expect("id가 1이면 일치하는 채팅방을 반환한다") {
            val foundRoom = roomRepository.findById(ONE)
            foundRoom?.run {
                id shouldBe ONE
                name shouldBe "Team small talk 입니다~"
                idQueue?.size shouldBe 10
                members?.size?.shouldBeZero()
            }
        }
        expect("id가 일치하는 채팅방이 없으면 null 값을 반환한다") {
            roomRepository.findById(ONE).shouldBeNull()
        }
    }

    context("모든 채팅방을 조회할 경우") {
        repeat(3) {
            roomRepository.save("채팅방$it")
        }
        expect("채팅방이 1개 이상 존재하면 모든 채팅방을 반환한다") {
            roomRepository.findAll().size shouldBe 3
        }
        expect("채팅방이 존재하지 않는다면 비어있는 리스트를 반환한다") {
            roomRepository.findAll().size.shouldBeZero()
        }
    }

    context("id가 일치하는 채팅방을 삭제할 경우") {
        roomRepository.save("Team small talk 입니다~")
        expect("id가 1이면 일치하는 채팅방을 삭제한다") {
            roomRepository.run {
                deleteById(ONE)
                findById(ONE).shouldBeNull()
            }
        }
    }

    context("모든 채팅방을 삭제할 경우") {
        repeat(3) {
            roomRepository.save("채팅방$it")
        }
        expect("채팅방이 1개 이상 존재하면 모든 채팅방을 삭제한다") {
            roomRepository.run {
                deleteAll()
                findAll().size.shouldBeZero()
            }
        }
    }

    context("채팅방에 입장할 경우") {
        val foundRoom =
            roomRepository.run {
                save("siuuuuu")
                findById(ONE)
            }
        expect("입장한 채팅방을 반환한다") {
            val updatedRoom =
                foundRoom?.let {
                    roomRepository.addMember(it)
                }
            updatedRoom?.run {
                idQueue?.size shouldBe 9
                members?.size shouldBe 1
                members?.last() shouldBe 1
            }
        }
    }

    context("채팅방 정보를 갱신할 경우") {
        val updatedRoom =
            roomRepository.run {
                save("안뇽!")
                findById(ONE)?.let {
                    addMember(it)
                }
            }
        expect("정보가 갱신된 채팅방을 저장한다") {
            updatedRoom?.let {
                roomRepository.update(it)
            }
            val foundRoom = roomRepository.findById(ONE)
            foundRoom?.run {
                id shouldBe ONE
                idQueue?.size shouldBe 9
                members?.size shouldBe 1
                members?.last() shouldBe 1
            }
        }
    }

    context("채팅방에서 퇴장할 경우") {
        val foundRoom =
            roomRepository.run {
                update(
                    Room(
                        ONE,
                        "miuuuuu",
                        (2..10).toMutableList(),
                        mutableListOf(1)
                    )
                )
                findById(ONE)
            }
        expect("퇴장한 채팅방을 반환한다") {
            val updatedRoom =
                foundRoom?.let {
                    roomRepository.deleteMember(it, 1)
                }
            updatedRoom?.run {
                id shouldBe ONE
                idQueue?.size shouldBe 10
                members?.size?.shouldBeZero()
            }
        }
    }

    afterEach {
        roomRepository.deleteAll()
    }
})