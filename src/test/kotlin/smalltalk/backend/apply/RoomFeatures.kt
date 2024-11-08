package smalltalk.backend.apply

import smalltalk.backend.application.websocket.RoomEventListener
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
const val MEMBER_INIT = 1L
const val PROVIDER_INIT = 2L
const val PROVIDER_LIMIT = 10L
const val MEMBER_SESSION_ID = "session-id"
const val API_PREFIX = "/api/rooms"

fun create(id: Long = ID, name: String = NAME) = Room(id, name, MEMBER_INIT.toInt())

fun createRooms() = (1L..3L).map { create(it, NAME + it) }

fun createOpenRequest() = OpenRequest(NAME)

fun createOpenResponse() = OpenResponse(ID, MEMBER_INIT)

fun createSimpleInfoResponse() = (1L..3L).map { SimpleInfoResponse(it, NAME + it, MEMBER_INIT.toInt()) }

fun createEnterResponse() = EnterResponse(MEMBER_INIT)

fun createErrorResponseWhenEnter(code: String) = Error(code)

fun getDestination(id: Long) = WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX + id

fun getNickname(memberId: Long) = RoomEventListener.MEMBER_NICKNAME_PREFIX + memberId

fun getStringValue(client: ObjectMapperClient, value: Any) = client.getStringValue(value)

inline fun <reified T : Any> getExpectedValue(client: ObjectMapperClient, value: Any) =
    client.getExpectedValue(value, T::class.java)