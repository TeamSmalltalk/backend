package smalltalk.backend.presentation.dto.room

data class OpenRoomResponse(
    val roomId: Long,
    val memberId: Long
)

data class SimpleRoomInfoResponse(
    val id: Long,
    val name: String,
    val memberCount: Int
)