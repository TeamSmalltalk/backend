package smalltalk.backend.apply.controller.room

import com.ninjasquad.springmockk.MockkBean
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.FunSpec
import io.mockk.clearAllMocks
import io.mockk.every
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import smalltalk.backend.application.service.room.RoomService
import smalltalk.backend.apply.*
import smalltalk.backend.exception.room.advice.RoomExceptionSituationCode.DELETED
import smalltalk.backend.exception.room.advice.RoomExceptionSituationCode.FULL
import smalltalk.backend.exception.room.situation.FullRoomException
import smalltalk.backend.exception.room.situation.RoomNotFoundException
import smalltalk.backend.presentation.controller.room.RoomController
import smalltalk.backend.support.spec.afterRootTest
import smalltalk.backend.util.jackson.ObjectMapperClient

@WebMvcTest(RoomController::class)
@Import(ObjectMapperClient::class)
class RoomControllerTest(
    @MockkBean private val roomService: RoomService,
    private val mockMvc: MockMvc,
    private val client: ObjectMapperClient
) : FunSpec({
    val logger = KotlinLogging.logger { }

    test("채팅방 생성 요청에 대하여 응답으로 생성된 채팅방과 멤버 정보가 반환된다") {
        val response = createOpenResponse()
        every { roomService.open(any()) } returns response
        mockMvc.post("/api/rooms") {
            contentType = APPLICATION_JSON
            content = client.getStringValue(createOpenRequest())
        }.andExpect {
            status { isCreated() }
            content { json(getStringValue(client, response), true) }
        }
    }

    test("모든 채팅방을 조회한다") {
        val response = createSimpleInfoResponse()
        every { roomService.getSimpleInfos() } returns response
        mockMvc.get("/api/rooms").andExpect {
            status { isOk() }
            content { json(getStringValue(client, response), true) }
        }
    }

    test("채팅방 입장 요청에 대하여 응답으로 생성된 멤버 정보가 반환된다") {
        val response = createEnterResponse()
        every { roomService.enter(any()) } returns response
        mockMvc.post("/api/rooms/$ID").andExpect {
            status { isOk() }
            content { json(getStringValue(client, response), true) }
        }
    }

    test("이미 삭제된 채팅방 입장 요청에 대하여 응답으로 에러 코드 601이 반환된다") {
        every { roomService.enter(any()) } throws RoomNotFoundException()
        mockMvc.post("/api/rooms/$ID").andExpect {
            status { isNotFound() }
            content { json(getStringValue(client, createErrorResponseWhenEnter(DELETED.code)), true) }
        }
    }

    test("가득찬 채팅방 입장 요청에 대하여 응답으로 에러 코드 602가 반환된다") {
        every { roomService.enter(any()) } throws FullRoomException()
        mockMvc.post("/api/rooms/$ID").andExpect {
            status { isBadRequest() }
            content { json(getStringValue(client, createErrorResponseWhenEnter(FULL.code)), true) }
        }
    }

    afterRootTest {
        clearAllMocks()
    }
})