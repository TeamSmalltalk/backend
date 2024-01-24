package smalltalk.backend.application.implement.message

import smalltalk.backend.presentation.dto.chatmessage.ChatMessage

class SimpleMessageBroker : MessageBroker {
    override fun send(chatRoomId: Long, chatMessage: ChatMessage) {
        TODO("Not yet implemented")
    }
}