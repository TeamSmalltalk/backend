package smalltalk.backend.application.implement.room

import smalltalk.backend.presentation.dto.room.OpenResponse

class RoomResponseMapper {
    companion object {
        fun toOpenResponse(roomId: Long, memberId: Long) =
            OpenResponse(
                roomId,
                memberId
            )
    }
}