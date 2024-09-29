package smalltalk.backend.apply

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.getForEntity
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus.CREATED
import org.springframework.http.HttpStatus.OK
import org.springframework.test.annotation.DirtiesContext
import smalltalk.backend.BackendApplication
import smalltalk.backend.infra.repository.room.RoomRepository
import smalltalk.backend.presentation.dto.room.request.OpenRequest
import smalltalk.backend.presentation.dto.room.response.OpenResponse
import smalltalk.backend.presentation.dto.room.response.SimpleInfoResponse
import smalltalk.backend.support.redis.RedisContainerConfig
import smalltalk.backend.support.spec.afterRootTest

@SpringBootTest(classes = [BackendApplication::class], webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(RedisContainerConfig::class)
@DirtiesContext
class BackendApplicationIntegrationTest(
    private val roomRepository: RoomRepository,
    private val template: TestRestTemplate
) : FunSpec({
    val logger = KotlinLogging.logger { }

    test("채팅방 생성 요청에 대하여 응답으로 생성된 채팅방과 멤버 정보가 반환된다") {
        val response = template.postForEntity<OpenResponse>("/api/rooms", OpenRequest(NAME))
        response.run {
            statusCode shouldBe CREATED
            body?.run {
                id shouldBe ID
                memberId shouldBe MEMBERS_INITIAL_ID
            }
        }
    }

    test("모든 채팅방을 조회한다") {
        (1..3).map { roomRepository.save(NAME + it) }
        val response = template.getForEntity<List<SimpleInfoResponse>>("/api/rooms")
        response.run {
            statusCode shouldBe OK
            body?.shouldHaveSize(3)
        }
    }

    afterRootTest {
        roomRepository.deleteAll()
    }
})