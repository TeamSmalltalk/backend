package smalltalk.backend.application.implement.message

import smalltalk.backend.presentation.dto.chatmessage.ChatMessage

interface MessageBroker {
    fun send(chatRoomId: Long, chatMessage: ChatMessage)
}