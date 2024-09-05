package smalltalk.backend.presentation.dto.message

data class Entrance(
    val numberOfMember: Int,
    val nickname: String,
    val text: String
)

data class Exit(
    val numberOfMember: Int,
    val text: String
)

data class Chat(
    val sender: String,
    val text: String
)