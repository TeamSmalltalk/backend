package smalltalk.backend.apply.application.websocket

import kotlinx.serialization.Serializable

@Serializable
data class Bot(
    val numberOfMember: Int,
    val text: String
)