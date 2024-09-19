package smalltalk.backend.config.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.stereotype.Component


@Component
class InboundChannelInterceptor: ChannelInterceptor {
    private val logger = KotlinLogging.logger { }

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        return message
    }
}