package smalltalk.backend.config.websocket

import org.springframework.stereotype.Component

@Component
class WebSocketSessionSet {

    private final val sessionSet: MutableSet<String> = linkedSetOf()

    fun addSession(sessionId: String) = sessionSet.add(sessionId)

    fun removeSession(sessionId: String) = sessionSet.remove(sessionId)

    fun getSessionNum() = sessionSet.size
}