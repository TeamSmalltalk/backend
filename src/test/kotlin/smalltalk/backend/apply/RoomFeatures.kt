package smalltalk.backend.apply

import smalltalk.backend.config.websocket.WebSocketConfig
import smalltalk.backend.domain.room.Room
import smalltalk.backend.presentation.dto.message.Error
import smalltalk.backend.presentation.dto.room.request.OpenRequest
import smalltalk.backend.presentation.dto.room.response.EnterResponse
import smalltalk.backend.presentation.dto.room.response.OpenResponse
import smalltalk.backend.presentation.dto.room.response.SimpleInfoResponse
import smalltalk.backend.util.jackson.ObjectMapperClient

const val ID = 1L
const val NAME = "room"
const val ID_QUEUE_INITIAL_ID = 2L
const val ID_QUEUE_LIMIT_ID = 10L
const val MEMBERS_INITIAL_ID = 1L
const val MEMBER_SESSION_ID = "session-id"
const val MEMBER_NICKNAME_PREFIX = "익명"

fun create(id: Long = ID, name: String = NAME) =
    Room(id, name, (ID_QUEUE_INITIAL_ID..ID_QUEUE_LIMIT_ID).toMutableList(), mutableListOf(MEMBERS_INITIAL_ID))

fun createRooms() = (1L..3L).map { create(it, NAME + it) }

fun createOpenRequest() = OpenRequest(NAME)

fun createOpenResponse() = OpenResponse(ID, MEMBERS_INITIAL_ID)

fun createSimpleInfoResponse() = (1L..3L).map { SimpleInfoResponse(it, NAME + it, MEMBERS_INITIAL_ID.toInt()) }

fun createEnterResponse() = EnterResponse(MEMBERS_INITIAL_ID)

fun createErrorResponseWhenEnter(code: String) = Error(code)

fun getDestination(id: Long) = WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX + id

fun getNickname(memberId: Long) = MEMBER_NICKNAME_PREFIX + memberId

fun getStringValue(client: ObjectMapperClient, value: Any) = client.getStringValue(value)

fun <T> getExpectedValue(client: ObjectMapperClient, value: Any, expectedType: Class<T>) =
    client.getExpectedValue(value, expectedType)