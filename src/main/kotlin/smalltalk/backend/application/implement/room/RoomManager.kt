package smalltalk.backend.application.implement.room

import org.springframework.stereotype.Component
import smalltalk.backend.infrastructure.repository.room.RoomRepository

@Component
class RoomManager(
    private val roomRepository: RoomRepository
) {

}