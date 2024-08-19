package smalltalk.backend.presentation.dto.room.response

data class Open(
    val roomId: Long,
    val memberId: Long
)

data class SimpleInfo(
    val id: Long,
    val name: String,
    val memberCount: Int
)