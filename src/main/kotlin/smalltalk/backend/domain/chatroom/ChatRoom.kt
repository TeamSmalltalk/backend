package smalltalk.backend.domain.chatroom

class ChatRoom(
    name: String
) {
    val id: Long? = null
    var name: String = name
        private set
    val memberTotalCount: Int = 0
}