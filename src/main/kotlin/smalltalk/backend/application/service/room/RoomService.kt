package smalltalk.backend.application.service.room

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import smalltalk.backend.infra.repository.room.RoomRepository
import smalltalk.backend.presentation.dto.room.request.OpenRequest
import smalltalk.backend.presentation.dto.room.response.EnterResponse
import smalltalk.backend.presentation.dto.room.response.OpenResponse
import smalltalk.backend.presentation.dto.room.response.SimpleInfoResponse

@Service
class RoomService(private val roomRepository: RoomRepository) {
    private val logger = KotlinLogging.logger { }

    fun open(request: OpenRequest) = roomRepository.save(request.name).run { OpenResponse(id, members.last()) }

    fun getSimpleInfos() = roomRepository.findAll().map { SimpleInfoResponse(it.id, it.name, it.members.size) }

    fun enter(id: Long) = EnterResponse(roomRepository.addMember(id))
}