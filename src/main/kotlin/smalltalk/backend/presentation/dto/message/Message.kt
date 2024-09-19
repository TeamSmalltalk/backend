package smalltalk.backend.presentation.dto.message

data class Bot(
    val numberOfMember: Int,
    val text: String
)

data class Chat(
    val sender: String,
    val text: String
)