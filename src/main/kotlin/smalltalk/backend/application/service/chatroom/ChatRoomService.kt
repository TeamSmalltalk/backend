package smalltalk.backend.application.service.chatroom

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import smalltalk.backend.application.implement.chatroom.ChatRoomManager
import smalltalk.backend.application.implement.chatroom.ChatRoomResponseMapper
import smalltalk.backend.application.implement.message.SimpleMessageBroker
import smalltalk.backend.presentation.dto.chatmessage.ChatMessage


@Service
class ChatRoomService (
    private val chatRoomManager: ChatRoomManager,
    private val chatRoomResponseMapper: ChatRoomResponseMapper,
    private val simpleMessageBroker: SimpleMessageBroker
) {
    
}