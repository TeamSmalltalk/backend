package smalltalk.backend.domain.room

import java.io.Serializable

class Room(
    val id: Long?,
    val name: String?,
    val idQueue: MutableList<Int>?,
    val members: MutableList<Int>?
) : Serializable