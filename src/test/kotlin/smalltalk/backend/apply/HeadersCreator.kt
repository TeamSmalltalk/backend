package smalltalk.backend.apply

import org.hildan.krossbow.stomp.headers.StompSubscribeHeaders
import smalltalk.backend.application.websocket.MessageHeader.*

fun createHeaders(destination: String, type: String?, memberId: String?): StompSubscribeHeaders {
    val headers = mutableMapOf<String, String>()
    type?.let { headers[TYPE.key] = it }
    memberId?.let { headers[MEMBER.key] = memberId }
    return StompSubscribeHeaders(destination).apply { putAll(headers) }
}