package smalltalk.backend.apply.application.websocket

import kotlinx.serialization.Serializable

@Serializable
data class TestChat(
    val sender: String,
    val text: String
)