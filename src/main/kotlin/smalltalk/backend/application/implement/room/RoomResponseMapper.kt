package smalltalk.backend.application.implement.room

import smalltalk.backend.presentation.dto.room.response.Open
import smalltalk.backend.presentation.dto.room.response.SimpleInfo

fun toOpen(roomId: Long, memberId: Long) =
    Open(
        roomId,
        memberId
    )

fun toSimpleInfo(id: Long, name: String, memberCount: Int) =
    SimpleInfo(
        id,
        name,
        memberCount
    )