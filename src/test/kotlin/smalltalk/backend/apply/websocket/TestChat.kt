package smalltalk.backend.apply.websocket

import kotlinx.serialization.Serializable

@Serializable
data class TestChat(
    val sender: String,
    val text: String
)