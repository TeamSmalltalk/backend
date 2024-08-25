package smalltalk.backend.domain.room

class Room(
    val id: Long,
    val name: String,
    val idQueue: MutableList<Long>,
    val members: MutableList<Long>
)