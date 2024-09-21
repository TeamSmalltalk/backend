package smalltalk.backend.apply.websocket

import kotlinx.serialization.Serializable

@Serializable
data class Bot(
    val numberOfMember: Int,
    val text: String
)