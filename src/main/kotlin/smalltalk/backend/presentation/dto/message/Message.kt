package smalltalk.backend.presentation.dto.message

data class System(
    val numberOfMember: Int,
    val text: String
)

data class Chat(
    val sender: String,
    val text: String
)

data class Error(val code: String)