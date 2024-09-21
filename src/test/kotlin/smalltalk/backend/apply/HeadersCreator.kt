package smalltalk.backend.apply

import org.hildan.krossbow.stomp.headers.StompSubscribeHeaders
import smalltalk.backend.application.room.RoomEventListener

fun createHeaders(destination: String, type: String, memberId: String) =
    StompSubscribeHeaders(destination).apply {
        putAll(
            mapOf(
                RoomEventListener.TYPE_HEADER to type,
                RoomEventListener.MEMBER_ID_HEADER to memberId
            )
        )
    }