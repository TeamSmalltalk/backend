package smalltalk.backend.application.service.room

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import smalltalk.backend.infra.repository.room.RoomRepository
import smalltalk.backend.presentation.dto.room.request.OpenRequest
import smalltalk.backend.presentation.dto.room.response.OpenResponse

@Service
class RoomService(private val roomRepository: RoomRepository) {
    private val logger = KotlinLogging.logger { }

    fun open(request: OpenRequest) = roomRepository.save(request.name).run { OpenResponse(id, members.last()) }
}