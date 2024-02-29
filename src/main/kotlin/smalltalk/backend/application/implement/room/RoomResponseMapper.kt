package smalltalk.backend.application.implement.room

import org.springframework.stereotype.Component
import smalltalk.backend.presentation.dto.room.openResponse

@Component
class RoomResponseMapper {
    fun toOpenResponse(roomId: Long, memberId: Long) =
        openResponse(
            roomId,
            memberId
        )
}