package smalltalk.backend.application.implement.room

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.MessageBuilder
import smalltalk.backend.util.message.MessageBroker
import smalltalk.backend.application.websocket.MessageHeader.*
import smalltalk.backend.application.websocket.SystemType
import smalltalk.backend.application.websocket.SystemType.*
import smalltalk.backend.domain.room.Room
import smalltalk.backend.infra.repository.member.MemberRepository
import smalltalk.backend.infra.repository.room.RoomRepository
import smalltalk.backend.presentation.dto.message.Error
import smalltalk.backend.presentation.dto.message.System
import smalltalk.backend.presentation.dto.message.SystemTextPostfix
import smalltalk.backend.presentation.dto.room.response.Enter
import smalltalk.backend.presentation.dto.room.response.Open
import smalltalk.backend.presentation.dto.room.response.SimpleInfo
import smalltalk.backend.util.jackson.ObjectMapperClient

@Implement
class RoomImplementImpl(
    @Qualifier("clientOutboundChannel")
    private val outboundChannel: MessageChannel,
    private val roomRepository: RoomRepository,
    private val memberRepository: MemberRepository,
    private val broker: MessageBroker,
    private val client: ObjectMapperClient
) : RoomImplement {
    private val logger = KotlinLogging.logger { }
    companion object {
        private const val MEMBER_NICKNAME_PREFIX = "익명"
        private const val SUBSCRIBE_COMMON_EXCEPTION_CODE = "600"
    }

    override fun save(name: String) = roomRepository.save(name).run { Open(id, members.last()) }

    override fun getById(id: Long) = roomRepository.getById(id)

    override fun findAll() = roomRepository.findAll().map { SimpleInfo(it.id, it.name, it.members.size) }

    override fun addMember(id: Long) = Enter(roomRepository.addMember(id))

    override fun deleteMember(sessionId: String, id: Long, memberId: Long): Room? {
        memberRepository.deleteById(sessionId)
        return roomRepository.deleteMember(id, memberId)
    }

    override fun saveMember(sessionId: String, memberId: Long, id: Long) = memberRepository.save(sessionId, memberId, id)

    override fun findMemberById(sessionId: String) = memberRepository.findById(sessionId)

    override fun sendSystemMessage(topic: String, type: SystemType, numberOfMember: Int, name: String, memberId: Long) {
        send(topic, getSystemMessage(type, numberOfMember, name, memberId))
    }

    override fun sendChatMessage(topic: String, message: Any) {
        send(topic, message)
    }

    override fun sendErrorMessage(causedExceptionMessage: String?, sessionId: String, subscriptionId: String) {
        outboundChannel.send(createErrorMessage(createErrorMessagePayloadByCase(causedExceptionMessage), sessionId, subscriptionId))
    }

    private fun getSystemMessage(type: SystemType, numberOfMember: Int, name: String, memberId: Long) =
        when (type) {
            OPEN -> createSystemMessage(numberOfMember, name + SystemTextPostfix.OPEN)
            ENTER -> createSystemMessage(numberOfMember, MEMBER_NICKNAME_PREFIX + memberId + SystemTextPostfix.ENTER)
            EXIT -> createSystemMessage(numberOfMember, MEMBER_NICKNAME_PREFIX + memberId + SystemTextPostfix.EXIT)
        }

    private fun createSystemMessage(numberOfMember: Int, text: String) = System(numberOfMember, text)

    private fun createErrorMessage(payload: Error, sessionId: String, subscriptionId: String) =
        MessageBuilder.createMessage(
            client.getByteArrayValue(payload),
            StompHeaderAccessor.create(StompCommand.MESSAGE).apply {
                this.sessionId = sessionId
                this.subscriptionId = subscriptionId
            }.messageHeaders
        )

    private fun createErrorMessagePayloadByCase(causedExceptionMessage: String?) =
        when (causedExceptionMessage) {
            SESSION.key -> createErrorMessagePayload(SESSION.code)
            SUBSCRIPTION.key -> createErrorMessagePayload(SUBSCRIPTION.code)
            ROOM.key -> createErrorMessagePayload(ROOM.code)
            MEMBER.key -> createErrorMessagePayload(MEMBER.code)
            TYPE.key -> createErrorMessagePayload(TYPE.code)
            else -> createErrorMessagePayload(SUBSCRIBE_COMMON_EXCEPTION_CODE)
        }

    private fun createErrorMessagePayload(code: String) = Error(code)

    private fun send(topic: String, message: Any) {
        broker.send(topic, message)
    }
}