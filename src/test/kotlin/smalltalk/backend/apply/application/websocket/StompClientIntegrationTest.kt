package smalltalk.backend.apply.application.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.ExpectSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.conversions.kxserialization.convertAndSend
import org.hildan.krossbow.stomp.conversions.kxserialization.json.withJsonConversions
import org.hildan.krossbow.websocket.spring.asKrossbowWebSocketClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import smalltalk.backend.application.websocket.MessageHeader.*
import smalltalk.backend.application.websocket.SystemType.ENTER
import smalltalk.backend.application.websocket.SystemType.OPEN
import smalltalk.backend.apply.MEMBER_NICKNAME_PREFIX
import smalltalk.backend.apply.NAME
import smalltalk.backend.apply.createHeaders
import smalltalk.backend.config.websocket.WebSocketConfig
import smalltalk.backend.infra.repository.member.MemberRepository
import smalltalk.backend.infra.repository.room.RoomRepository
import smalltalk.backend.presentation.dto.message.Chat
import smalltalk.backend.presentation.dto.message.Error
import smalltalk.backend.presentation.dto.message.System
import smalltalk.backend.presentation.dto.message.SystemTextPostfix
import smalltalk.backend.support.redis.RedisContainerConfig
import smalltalk.backend.support.spec.afterRootTest
import smalltalk.backend.util.jackson.ObjectMapperClient

@ActiveProfiles("test")
@Import(RedisContainerConfig::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
class StompClientIntegrationTest(
    @LocalServerPort
    private val port: Int,
    private val roomRepository: RoomRepository,
    private val memberRepository: MemberRepository,
    private val mapperClient: ObjectMapperClient
) : ExpectSpec({
    val logger = KotlinLogging.logger { }
    val url = "ws://localhost:$port${WebSocketConfig.STOMP_ENDPOINT}"
    val client = StompClient(StandardWebSocketClient().asKrossbowWebSocketClient())

    context("채팅방 생성") {
        val room = roomRepository.save(NAME)
        val destination = WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX + room.id
        expect("메시지를 수신한다") {
            val session = client.connect(url)
            val message = mapperClient.getExpectedValue(
                session.subscribe(createHeaders(destination, OPEN.name, room.members.last().toString())).first().bodyAsText,
                System::class.java
            )
            message.run {
                numberOfMember shouldBe 1
                text shouldBe (NAME + SystemTextPostfix.OPEN)
            }
            session.disconnect()
        }
    }

    context("채팅방 입장") {
        val room = roomRepository.save(NAME)
        val destination = WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX + room.id
        val enteredMemberId = roomRepository.addMember(room.id)
        expect("입장 메시지를 수신한다") {
            val session = client.connect(url)
            val message = mapperClient.getExpectedValue(
                session.subscribe(createHeaders(destination, ENTER.name, enteredMemberId.toString())).first().bodyAsText,
                System::class.java
            )
            message.run {
                numberOfMember shouldBe 2
                text shouldBe (MEMBER_NICKNAME_PREFIX + enteredMemberId + SystemTextPostfix.ENTER)
            }
            session.disconnect()
        }
        expect("주소가 존재하지 않는다면 에러 메시지를 수신한다") {
            val session = client.connect(url)
            val message = mapperClient.getExpectedValue(
                session.subscribe(
                    createHeaders(
                        "${WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX}abc",
                        ENTER.name,
                        enteredMemberId.toString()
                    )
                ).first().bodyAsText,
                Error::class.java
            )
            message.code shouldBe ROOM.code
            session.disconnect()
        }
        expect("입장한 멤버가 존재하지 않는다면 에러 메시지를 수신한다") {
            val session = client.connect(url)
            val message = mapperClient.getExpectedValue(
                session.subscribe(createHeaders(destination, ENTER.name, null)).first().bodyAsText,
                Error::class.java
            )
            message.code shouldBe MEMBER.code
            session.disconnect()
        }
        expect("생성 또는 입장을 구분하는 타입이 존재하지 않는다면 에러 메시지를 수신한다") {
            val session = client.connect(url)
            val message = mapperClient.getExpectedValue(
                session.subscribe(createHeaders(destination, null, enteredMemberId.toString())).first().bodyAsText,
                Error::class.java
            )
            message.code shouldBe TYPE.code
            session.disconnect()
        }
    }

    context("채팅방 퇴장") {
        val room = roomRepository.save(NAME)
        val destination = WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX + room.id
        val messageChannel = Channel<System>()
        val messages = mutableListOf<System>()
        val sessionToOpenRoom = client.connect(url)
        launch {
            sessionToOpenRoom.subscribe(createHeaders(destination, OPEN.name, room.members.last().toString()))
                .take(3)
                .collect {
                    val message = mapperClient.getExpectedValue(it.bodyAsText, System::class.java)
                    messages.add(message)
                    messageChannel.send(message)
                }
        }
        messageChannel.receive()
        val enteredMemberId = roomRepository.addMember(room.id)
        expect("메시지를 수신한다") {
            val sessionToEnterRoom = client.connect(url)
            sessionToEnterRoom.subscribe(createHeaders(destination, ENTER.name, enteredMemberId.toString())).first()
            repeat(2) {
                messageChannel.receive()
            }
            messages.last().run {
                numberOfMember shouldBe 1
                text shouldBe (MEMBER_NICKNAME_PREFIX + enteredMemberId + SystemTextPostfix.EXIT)
            }
            sessionToEnterRoom.disconnect()
            sessionToOpenRoom.disconnect()
        }
    }

    context("채팅방 메시지 전송") {
        val room = roomRepository.save(NAME)
        val destinationToSubscribeRoom = WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX + room.id
        val enteredMemberId = room.members.last()
        val messageChannel = Channel<String>()
        val session = client.connect(url).withJsonConversions()
        launch {
            session.subscribe(createHeaders(destinationToSubscribeRoom, OPEN.name, enteredMemberId.toString()))
                .take(2)
                .collect { messageChannel.send(it.bodyAsText) }
        }
        messageChannel.receive()
        expect("메시지를 수신한다") {
            val destinationToSendChatMessage = WebSocketConfig.SEND_DESTINATION_PREFIX + room.id
            val sender = MEMBER_NICKNAME_PREFIX + enteredMemberId
            val text = "안녕하세요!"
            session.convertAndSend(destinationToSendChatMessage, TestChat(sender, text), TestChat.serializer())
            val message = mapperClient.getExpectedValue(messageChannel.receive(), Chat::class.java)
            message.let {
                it.sender shouldBe sender
                it.text shouldBe text
            }
            session.disconnect()
        }
    }

    afterRootTest {
        roomRepository.deleteAll()
        memberRepository.deleteAll()
    }
})