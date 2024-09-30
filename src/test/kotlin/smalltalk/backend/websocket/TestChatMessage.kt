package smalltalk.backend.websocket

import kotlinx.serialization.Serializable

@Serializable
data class TestChatMessage(
    val sender: String,
    val text: String
)