package smalltalk.backend.config.websocket

import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.stereotype.Component


@Component
class InboundChannelInterceptor: ChannelInterceptor {
    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        return message
    }
}