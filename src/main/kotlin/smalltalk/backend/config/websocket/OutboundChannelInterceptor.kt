package smalltalk.backend.config.websocket

import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.SimpMessageType
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.stereotype.Component
import smalltalk.backend.logger

@Component
class OutboundChannelInterceptor: ChannelInterceptor {
    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        if (StompHeaderAccessor.wrap(message).command == StompCommand.RECEIPT)
            logger.info { "Receipt $message" }
        if (message.headers["simpMessageType"] == SimpMessageType.MESSAGE) {
            logger.info { "Message $message" }
        }
        return message
    }
}