package smalltalk.backend.application.implement.room

import smalltalk.backend.presentation.dto.room.OpenRoomResponse
import smalltalk.backend.presentation.dto.room.SimpleRoomInfoResponse

fun toOpenRoomResponse(roomId: Long, memberId: Long) =
    OpenRoomResponse(
        roomId,
        memberId
    )

fun toSimpleRoomInfoResponse(id: Long, name: String, memberCount: Int) =
    SimpleRoomInfoResponse(
        id,
        name,
        memberCount
    )