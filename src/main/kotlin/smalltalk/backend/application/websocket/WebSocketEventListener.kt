package smalltalk.backend.application.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.event.EventListener
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionSubscribeEvent


@Component
class WebSocketEventListener (
    @Qualifier("clientOutboundChannel") private val outboundChannel: MessageChannel
){
    @EventListener
    private fun handleSubscribe(event: SessionSubscribeEvent) {
        StompHeaderAccessor.create(StompCommand.RECEIPT).run {
            sessionId = StompHeaderAccessor.wrap(event.message).sessionId
            outboundChannel.send(
                MessageBuilder.createMessage(
                    "ENTER".toByteArray(),
                    messageHeaders
                )
            )
        }
    }
}