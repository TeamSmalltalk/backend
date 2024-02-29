package smalltalk.backend.presentation.dto.room

data class OpenResponse(
    val roomId: Long,
    val memberId: Long
)

data class SimpleInfoResponse(
    val id: Long,
    val name: String,
    val memberCount: Int
)