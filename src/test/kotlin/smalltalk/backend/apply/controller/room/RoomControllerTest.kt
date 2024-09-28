package smalltalk.backend.apply.controller.room

import com.ninjasquad.springmockk.MockkBean
import io.github.oshai.kotlinlogging.KotlinLogging
import io.mockk.every
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post
import smalltalk.backend.application.service.room.RoomService
import smalltalk.backend.apply.*
import smalltalk.backend.exception.room.advice.RoomExceptionSituationCode.DELETED
import smalltalk.backend.exception.room.advice.RoomExceptionSituationCode.FULL
import smalltalk.backend.exception.room.situation.FullRoomException
import smalltalk.backend.exception.room.situation.RoomNotFoundException
import smalltalk.backend.presentation.controller.room.RoomController
import smalltalk.backend.util.jackson.ObjectMapperClient

@WebMvcTest(RoomController::class)
@Import(ObjectMapperClient::class)
class RoomControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc
    @Autowired
    lateinit var client: ObjectMapperClient
    @MockkBean
    lateinit var roomService: RoomService
    val logger = KotlinLogging.logger { }

    @Test
    fun `채팅방 생성 요청에 대하여 응답으로 생성된 채팅방과 멤버의 정보가 반환된다`() {
        val response = createOpenResponse()
        every { roomService.open(any()) } returns response
        mockMvc.post("/api/rooms") {
            contentType = APPLICATION_JSON
            content = getStringValue(createOpenRequest())
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
            content { json(getStringValue(response), true) }
        }
    }

    @Test
    fun `채팅방 입장 요청에 대하여 응답으로 멤버의 정보가 반환된다`() {
        val response = createEnterResponse()
        every { roomService.enter(any()) } returns response
        mockMvc.patch("/api/rooms/$ID").andExpect {
            status { isOk() }
            content { json(getStringValue(response), true) }
        }
    }

    @Test
    fun `이미 삭제된 채팅방 입장 요청에 대하여 응답으로 에러 코드 601이 반환된다`() {
        every { roomService.enter(any()) } throws RoomNotFoundException()
        mockMvc.patch("/api/rooms/$ID").andExpect {
            status { isNotFound() }
            content { json(getStringValue(createErrorResponseWhenEnter(DELETED.code)), true) }
        }
    }

    @Test
    fun `가득찬 채팅방 입장 요청에 대하여 응답으로 에러 코드 602가 반환된다`() {
        every { roomService.enter(any()) } throws FullRoomException()
        mockMvc.patch("/api/rooms/$ID").andExpect {
            status { isBadRequest() }
            content { json(getStringValue(createErrorResponseWhenEnter(FULL.code)), true) }
        }
    }

    private fun getStringValue(value: Any) = client.getStringValue(value)
}