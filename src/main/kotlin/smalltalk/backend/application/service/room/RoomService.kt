package smalltalk.backend.application.service.room

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import smalltalk.backend.infrastructure.repository.room.RoomRepository
import smalltalk.backend.presentation.dto.room.request.OpenRequest
import smalltalk.backend.presentation.dto.room.response.EnterResponse
import smalltalk.backend.presentation.dto.room.response.OpenResponse
import smalltalk.backend.presentation.dto.room.response.SimpleInfoResponse

@Service
class RoomService(private val roomRepository: RoomRepository) {
    private val logger = KotlinLogging.logger { }

    fun open(request: OpenRequest) = roomRepository.save(request.name).let { OpenResponse(it.id, it.numberOfMember.toLong()) }

    fun getSimpleInfos() = roomRepository.findAll().map { SimpleInfoResponse(it.id, it.name, it.numberOfMember) }

    fun enter(id: String) = EnterResponse(roomRepository.addMember(id.toLong()))
}