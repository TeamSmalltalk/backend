package smalltalk.backend.apply

import org.hildan.krossbow.stomp.headers.StompSubscribeHeaders
import smalltalk.backend.application.room.RoomEventListener

fun createHeaders(destination: String, type: String?, memberId: String?): StompSubscribeHeaders {
    val header = mutableMapOf<String, String>()
    type?.let { header[RoomEventListener.TYPE_HEADER] = it }
    memberId?.let { header[RoomEventListener.MEMBER_ID_HEADER] = memberId }
    return StompSubscribeHeaders(destination).apply { putAll(header) }
}