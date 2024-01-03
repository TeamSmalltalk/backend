package smalltalk.backend

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service


@Service
class ChattingService (
    private val simpMessagingTemplate: SimpMessagingTemplate
) {

    fun send(roomId: String, chattingMessage: ChattingMessage) {
        simpMessagingTemplate.convertAndSend(roomId, chattingMessage)
    }
}