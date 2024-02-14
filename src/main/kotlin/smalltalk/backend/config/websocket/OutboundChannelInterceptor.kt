package smalltalk.backend.config.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.SimpMessageType
import org.springframework.messaging.simp.SimpMessageType.*
import org.springframework.messaging.simp.stomp.StompClientSupport
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompCommand.*
import org.springframework.messaging.simp.stomp.StompCommand.CONNECT
import org.springframework.messaging.simp.stomp.StompCommand.DISCONNECT
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.simp.stomp.StompTcpMessageCodec
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.stereotype.Component

@Component
class OutboundChannelInterceptor: ChannelInterceptor {

    private val logger = KotlinLogging.logger {}

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        return message
    }

    override fun postSend(message: Message<*>, channel: MessageChannel, sent: Boolean) {
    }
}
