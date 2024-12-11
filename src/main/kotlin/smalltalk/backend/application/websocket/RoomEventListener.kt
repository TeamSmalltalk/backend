package smalltalk.backend.application.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.event.EventListener
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionSubscribeEvent
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent
import smalltalk.backend.application.websocket.MessageHeader.*
import smalltalk.backend.application.websocket.SystemType.*
import smalltalk.backend.config.websocket.WebSocketConfig
import smalltalk.backend.exception.room.situation.DOESNT_EXIST_HEADER_MESSAGE_PREFIX
import smalltalk.backend.exception.room.situation.DoesntExistHeaderException
import smalltalk.backend.infrastructure.repository.member.MemberRepository
import smalltalk.backend.infrastructure.repository.room.RoomRepository
import smalltalk.backend.infrastructure.repository.room.getById
import smalltalk.backend.presentation.dto.message.Error
import smalltalk.backend.presentation.dto.message.System
import smalltalk.backend.presentation.dto.message.SystemTextPostfix
import smalltalk.backend.util.jackson.ObjectMapperClient
import smalltalk.backend.util.message.MessageBroker

@Component
class RoomEventListener(
    @Qualifier("clientOutboundChannel") private val outboundChannel: MessageChannel,
    private val roomRepository: RoomRepository,
    private val memberRepository: MemberRepository,
    private val broker: MessageBroker,
    private val mapper: ObjectMapperClient
) {
    companion object {
        const val MEMBER_NICKNAME_PREFIX = "익명"
        private const val DESTINATION_PATTERN = "^${WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX}[0-9]+$"
        private const val ROOM_ID_START_INDEX = 7
        private const val SUBSCRIBE_COMMON_EXCEPTION_CODE = "600"
    }
    private val logger = KotlinLogging.logger { }

    @EventListener
    private fun handleSubscribe(event: SessionSubscribeEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)
        val sessionId = getValue(accessor.sessionId, SESSION.key)
        val subscriptionId = getValue(accessor.subscriptionId, SUBSCRIPTION.key)
        try {
            val destination = getDestination(accessor.destination, ROOM.key)
            val id = getRoomId(destination).toLong()
            val memberId = getNativeHeaderValue(accessor, MEMBER.key).toLong()
            val room = roomRepository.getById(id)
            when (getNativeHeaderValue(accessor, TYPE.key)) {
                OPEN.name -> sendSystemMessage(destination, OPEN, room.numberOfMember, room.name, memberId)
                ENTER.name -> sendSystemMessage(destination, ENTER, room.numberOfMember, room.name, memberId)
                else -> throw DoesntExistHeaderException(TYPE.key)
            }
            memberRepository.save(sessionId, memberId, id)
        } catch (exceptionToHandle: Exception) {
            var message = exceptionToHandle.message
            message?.let {
                if (checkExceptionMessagePrefix(it))
                    message = getExceptionCause(it)
            }
            sendErrorMessage(message, sessionId, subscriptionId)
        }
    }

    @EventListener
    private fun handleUnsubscribe(event: SessionUnsubscribeEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)
        val sessionId = getValue(accessor.sessionId, SESSION.key)
        memberRepository.findById(sessionId)?.let { member ->
            val id = member.roomId
            val memberIdToDelete = member.id
            memberRepository.deleteById(sessionId)
            roomRepository.deleteMember(id, memberIdToDelete)?.let { room ->
                sendSystemMessage(getDestination(id), EXIT, room.numberOfMember, room.name, memberIdToDelete)
            }
        }
    }

    private fun getValue(valueToCheck: String?, nameIfCauseException: String) =
        valueToCheck ?: throw DoesntExistHeaderException(nameIfCauseException)

    private fun getDestination(destination: String?, nameIfCauseException: String) =
        destination?.takeIf { Regex(DESTINATION_PATTERN).matches(it) } ?: throw DoesntExistHeaderException(nameIfCauseException)

    private fun getDestination(id: Long) = WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX + id

    private fun getRoomId(destination: String) = destination.substring(ROOM_ID_START_INDEX)

    private fun getNativeHeaderValue(accessor: StompHeaderAccessor, key: String) =
        accessor.getFirstNativeHeader(key) ?: throw DoesntExistHeaderException(key)

    private fun sendSystemMessage(topic: String, type: SystemType, numberOfMember: Int, name: String, memberId: Long) {
        broker.send(topic, getSystemMessage(type, numberOfMember, name, memberId))
    }

    fun sendErrorMessage(causedExceptionMessage: String?, sessionId: String, subscriptionId: String) {
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
            mapper.getByteArrayValue(payload),
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

    private fun checkExceptionMessagePrefix(message: String) = message.startsWith(DOESNT_EXIST_HEADER_MESSAGE_PREFIX)

    private fun getExceptionCause(message: String) = message.substringAfter(DOESNT_EXIST_HEADER_MESSAGE_PREFIX)
}