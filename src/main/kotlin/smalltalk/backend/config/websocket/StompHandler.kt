package smalltalk.backend.config.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand.*
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.stereotype.Component

@Component
class StompHandler (
    private val webSocketSessionSet: WebSocketSessionSet
): ChannelInterceptor {

    private val logger = KotlinLogging.logger {}

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? =
        message.also {
            val header = StompHeaderAccessor.wrap(it)
            when (header.command) {
                CONNECT -> handleConnect(header.sessionId)
                DISCONNECT -> handleDisconnect(header.sessionId)
                else -> logger.info { "not yet be implemented command handling" }
            }
        }

    override fun postSend(message: Message<*>, channel: MessageChannel, sent: Boolean) {
        if (!sent) logger.info { "sent failed" }
    }

    private fun handleConnect(sessionId: String?) {
        sessionId?.let { webSocketSessionSet.addSession(sessionId) }
        logger.info { "save new session $sessionId" }
    }

    private fun handleDisconnect(sessionId: String?) {
        sessionId?.let { webSocketSessionSet.removeSession(sessionId) }
        logger.info { "remove session $sessionId" }
    }
}