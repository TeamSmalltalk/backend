package smalltalk.backend.presentation.dto.room.response

data class OpenResponse(
    val id: Long,
    val memberId: Long
)

data class SimpleInfoResponse(
    val id: Long,
    val name: String,
    val numberOfMember: Int
)

data class EnterResponse(val memberId: Long)