package smalltalk.backend.application.implement.room

import smalltalk.backend.presentation.dto.room.OpenResponse
import smalltalk.backend.presentation.dto.room.SimpleInfoResponse

fun toOpenResponse(roomId: Long, memberId: Long) =
    OpenResponse(
        roomId,
        memberId
    )

fun toSimpleInfoResponse(id: Long, name: String, memberCount: Int) =
    SimpleInfoResponse(
        id,
        name,
        memberCount
    )