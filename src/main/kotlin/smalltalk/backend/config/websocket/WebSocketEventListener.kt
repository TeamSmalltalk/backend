package smalltalk.backend.config.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessageHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import org.springframework.web.socket.messaging.SessionSubscribeEvent
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent
import smalltalk.backend.domain.session.WebSocketSessionStorage



//@Component
class WebSocketEventListener (
    private val webSocketSessionStorage: WebSocketSessionStorage
){

    private val logger = KotlinLogging.logger {  }

    @EventListener
    private fun handleSessionConnected(event: SessionConnectEvent) =
      SimpMessageHeaderAccessor.wrap(event.message).sessionId ?.let {
          logger.info { "connect new session $it" }
          webSocketSessionStorage.addSession(it)
      }

    @EventListener
    private fun handleSessionDisconnect(event: SessionDisconnectEvent) =
        SimpMessageHeaderAccessor.wrap(event.message).sessionId ?.let {
            logger.info { "disconnect session $it" }
            webSocketSessionStorage.removeSession(it)
        }

    @EventListener
    private fun handleSubscribe(event: SessionSubscribeEvent) =
        SimpMessageHeaderAccessor.wrap(event.message).apply {
            sessionId?.let {
                logger.info { "session $it subscribe $destination" }
            }
        }

    @EventListener
    private fun handleUnsubscribe(event: SessionUnsubscribeEvent) =
        SimpMessageHeaderAccessor.wrap(event.message).apply {
            sessionId?.let {
                logger.info { "session $it unsubscribe $destination" }
            }
        }
}
