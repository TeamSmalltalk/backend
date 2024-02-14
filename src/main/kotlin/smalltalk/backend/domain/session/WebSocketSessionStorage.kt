package smalltalk.backend.domain.session

import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap


@Component
class WebSocketSessionStorage {

    private val map: ConcurrentMap<String, LocalDateTime> = ConcurrentHashMap()

    fun addSession(sessionId: String) = map.put(sessionId, LocalDateTime.now())

    fun removeSession(sessionId: String) = map.remove(sessionId)

    fun getSessionEnterTime(sessionId: String) = map[sessionId]?.let { throw Exception() }

    fun getSessionCount() = map.size
}