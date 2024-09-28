package smalltalk.backend.presentation.dto.room.response

data class Open(
    val id: Long,
    val memberId: Long
)

data class SimpleInfo(
    val id: Long,
    val name: String,
    val numberOfMember: Int
)

data class Enter(val memberId: Long)