package smalltalk.backend.presentation.controller.chat

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import smalltalk.backend.application.service.chat.ChatService
import smalltalk.backend.presentation.dto.message.Chat

@Controller
class ChatController(private val chatService: ChatService) {
    private val logger = KotlinLogging.logger { }

    @MessageMapping("{id}")
    fun send(@DestinationVariable("id") id: String, message: Chat) {
        chatService.send(id, message)
    }
}