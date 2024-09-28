package smalltalk.backend.apply

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.client.postForEntity
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus.CREATED
import org.springframework.test.annotation.DirtiesContext
import smalltalk.backend.BackendApplication
import smalltalk.backend.presentation.dto.room.request.OpenRequest
import smalltalk.backend.presentation.dto.room.response.OpenResponse
import smalltalk.backend.support.redis.RedisContainerConfig

@SpringBootTest(
    classes = [BackendApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import(RedisContainerConfig::class)
@DirtiesContext
class ApplicationIntegrationTest {
    @Autowired
    lateinit var template: TestRestTemplate
    val logger = KotlinLogging.logger { }

    @Test
    fun `채팅방 생성 요청에 대하여 응답으로 생성된 채팅방과 멤버 정보가 반환된다`() {
        val response = template.postForEntity<OpenResponse>("/api/rooms", OpenRequest(NAME))
        response.run {
            statusCode shouldBe CREATED
            body?.run {
                id shouldBe ID
                memberId shouldBe MEMBERS_INITIAL_ID
            }
        }
    }
}