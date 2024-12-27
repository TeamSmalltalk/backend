package smalltalk.backend.apply.integration

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.http.HttpStatus.*
import smalltalk.backend.apply.*
import smalltalk.backend.exception.room.advice.RoomExceptionSituationCode.DELETED
import smalltalk.backend.exception.room.advice.RoomExceptionSituationCode.FULL
import smalltalk.backend.domain.room.RoomRepository
import smalltalk.backend.config.property.RoomYamlProperties
import smalltalk.backend.presentation.dto.message.Error
import smalltalk.backend.presentation.dto.room.request.OpenRequest
import smalltalk.backend.presentation.dto.room.response.EnterResponse
import smalltalk.backend.presentation.dto.room.response.OpenResponse
import smalltalk.backend.presentation.dto.room.response.SimpleInfoResponse
import smalltalk.backend.support.EnableTestContainers
import smalltalk.backend.support.spec.afterRootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(value = [RoomYamlProperties::class])
@EnableTestContainers
class RoomControllerIntegrationTest(
    private val roomRepository: RoomRepository,
    private val template: TestRestTemplate
) : FunSpec({
    val logger = KotlinLogging.logger { }

    test("채팅방 생성 요청에 대하여 응답으로 생성된 채팅방과 멤버 정보가 반환된다") {
        template.postForEntity<OpenResponse>(API_PREFIX, OpenRequest(NAME)).run {
            statusCode shouldBe CREATED
            body?.run {
                id shouldBe ID
                memberId shouldBe MEMBER_INIT
            }
        }
    }

    test("모든 채팅방을 조회한다") {
        (1..3).map { roomRepository.save(NAME + it) }
        template.getForEntity<List<SimpleInfoResponse>>(API_PREFIX).run {
            statusCode shouldBe OK
            body?.shouldHaveSize(3)
        }
    }

    test("채팅방 입장 요청에 대하여 응답으로 생성된 멤버 정보가 반환된다") {
        template.postForEntity<EnterResponse>("$API_PREFIX/${roomRepository.save(NAME).id}").run {
            statusCode shouldBe OK
            body?.memberId shouldBe 2L
        }
    }

    test("이미 삭제된 채팅방 입장 요청에 대하여 응답으로 에러 정보가 반환된다") {
        template.postForEntity<Error>("$API_PREFIX/$ID").run {
            statusCode shouldBe NOT_FOUND
            body?.code shouldBe DELETED.code
        }
    }

    test("가득찬 채팅방 입장 요청에 대하여 응답으로 에러 정보가 반환된다") {
        val savedRoom = roomRepository.save(NAME)
        repeat(MEMBER_LIMIT - MEMBER_INIT) { roomRepository.addMember(savedRoom.id) }
        template.postForEntity<Error>("$API_PREFIX/${savedRoom.id}").run {
            statusCode shouldBe BAD_REQUEST
            body?.code shouldBe FULL.code
        }
    }

    afterRootTest {
        roomRepository.deleteAll()
    }
})