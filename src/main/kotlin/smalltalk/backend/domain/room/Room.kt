package smalltalk.backend.domain.room

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

class Room @JsonCreator constructor(
    @JsonProperty("id")
    val id: Long?,
    @JsonProperty("name")
    val name: String?,
    @JsonProperty("idQueue")
    val idQueue: MutableList<Int>?,
    @JsonProperty("members")
    val members: MutableList<Int>?
)