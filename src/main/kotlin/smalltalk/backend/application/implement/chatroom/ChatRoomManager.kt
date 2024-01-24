package smalltalk.backend.application.implement.chatroom

import org.springframework.stereotype.Component
import smalltalk.backend.infrastructure.repository.ChatRoomRepository

@Component
class ChatRoomManager(
    private val chatRoomRepository: ChatRoomRepository
) {

}