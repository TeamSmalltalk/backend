package smalltalk.backend.config.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.SimpMessageType
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.stereotype.Component

@Component
class OutboundChannelInterceptor: ChannelInterceptor {

    private val logger = KotlinLogging.logger {}
    companion object {
        private const val SIMP_MESSAGE_TYPE_KEY = "simpMessageType"
    }

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        if (StompHeaderAccessor.wrap(message).command == StompCommand.RECEIPT)
            logger.info { "Receipt $message" }
        if (message.headers[SIMP_MESSAGE_TYPE_KEY] == SimpMessageType.MESSAGE) {
            logger.info { "Message $message" }
        }
        return message
    }
}
