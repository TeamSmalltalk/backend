package smalltalk.backend.apply

import smalltalk.backend.domain.room.Room
import smalltalk.backend.presentation.dto.room.request.OpenRequest
import smalltalk.backend.presentation.dto.room.response.OpenResponse

const val ID = 1L
const val NAME = "room"
const val ID_QUEUE_INITIAL_ID = 2L
const val ID_QUEUE_LIMIT_ID = 10L
const val MEMBERS_INITIAL_ID = 1L
const val MEMBER_SESSION_ID = "session-id"
const val MEMBER_NICKNAME_PREFIX = "익명"

fun create(id: Long = ID, name: String = NAME) =
    Room(id, name, (ID_QUEUE_INITIAL_ID..ID_QUEUE_LIMIT_ID).toMutableList(), mutableListOf(MEMBERS_INITIAL_ID))

fun createOpenRequest() = OpenRequest(NAME)

fun createOpenResponse() = OpenResponse(ID, MEMBERS_INITIAL_ID)