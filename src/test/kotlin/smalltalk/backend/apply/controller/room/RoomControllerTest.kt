package smalltalk.backend.apply.controller.room

import com.ninjasquad.springmockk.MockkBean
import io.github.oshai.kotlinlogging.KotlinLogging
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import smalltalk.backend.application.service.room.RoomService
import smalltalk.backend.apply.createOpenRequest
import smalltalk.backend.apply.createOpenResponse
import smalltalk.backend.apply.createSimpleInfoResponse
import smalltalk.backend.presentation.controller.room.RoomController
import smalltalk.backend.util.jackson.ObjectMapperClient

@WebMvcTest(RoomController::class)
@Import(ObjectMapperClient::class)
class RoomControllerTest {
    @Autowired lateinit var mockMvc: MockMvc
    @Autowired lateinit var client: ObjectMapperClient
    @MockkBean lateinit var roomService: RoomService
    val logger = KotlinLogging.logger { }

    @Test
    fun `채팅방 생성 요청에 대하여 응답으로 생성된 채팅방과 멤버의 정보가 반환된다`() {
        val request = createOpenRequest()
        val response = createOpenResponse()
        every { roomService.open(request) } returns response
        mockMvc.post("/api/rooms") {
            contentType = MediaType.APPLICATION_JSON
            content = getStringValue(request)
        }.andExpect {
            status { isCreated() }
            content { json(getStringValue(response), true) }
        }
    }

    @Test
    fun `모든 채팅방을 조회한다`() {
        val response = createSimpleInfoResponse()
        every { roomService.getSimpleInfos() } returns response
        mockMvc.get("/api/rooms").andExpect {
            status { isOk() }
            content { getStringValue(response) }
        }
    }

    private fun getStringValue(value: Any) = client.getStringValue(value)
}