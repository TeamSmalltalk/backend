package smalltalk.backend.domain.room

class Room(
    name: String
) {
    val id: Long? = null
    var name: String = name
        private set
    val memberTotalCount: Int = 0
}