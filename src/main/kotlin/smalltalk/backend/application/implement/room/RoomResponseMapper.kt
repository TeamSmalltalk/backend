package smalltalk.backend.application.implement.room

import smalltalk.backend.presentation.dto.room.openResponse

class RoomResponseMapper {
    companion object {
        fun toOpenResponse(roomId: Long, memberId: Long) =
            openResponse(
                roomId,
                memberId
            )
    }
}