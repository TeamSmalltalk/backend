package smalltalk.backend.apply.integration

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.conversions.kxserialization.convertAndSend
import org.hildan.krossbow.stomp.conversions.kxserialization.json.withJsonConversions
import org.hildan.krossbow.websocket.spring.asKrossbowWebSocketClient
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import smalltalk.backend.apply.*
import smalltalk.backend.application.websocket.MessageHeader.*
import smalltalk.backend.application.websocket.SystemType.ENTER
import smalltalk.backend.application.websocket.SystemType.OPEN
import smalltalk.backend.config.websocket.WebSocketConfig
import smalltalk.backend.domain.member.MemberRepository
import smalltalk.backend.domain.room.RoomRepository
import smalltalk.backend.config.property.RoomYamlProperties
import smalltalk.backend.presentation.dto.message.Chat
import smalltalk.backend.presentation.dto.message.Error
import smalltalk.backend.presentation.dto.message.System
import smalltalk.backend.presentation.dto.message.SystemTextPostfix
import smalltalk.backend.util.ObjectMapperClient
import smalltalk.backend.support.EnableTestContainers
import smalltalk.backend.support.spec.afterRootTest

/**
 * 테스트 이름 주의!!
 * 채팅방 구독 -> 채팅방 생성 & 입장
 * 채팅방 구독 취소 -> 채팅방 퇴장
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableConfigurationProperties(value = [RoomYamlProperties::class])
@EnableTestContainers
class WebSocketIntegrationTest(
    @LocalServerPort private val port: Int,
    private val roomRepository: RoomRepository,
    private val memberRepository: MemberRepository,
    private val mapperClient: ObjectMapperClient
) : FunSpec({
    val logger = KotlinLogging.logger { }
    val url = "ws://localhost:$port${WebSocketConfig.STOMP_ENDPOINT}"
    val client = StompClient(StandardWebSocketClient().asKrossbowWebSocketClient())

    test("올바른 정보를 포함하고 채팅방을 구독하거나 취소하면 멤버들이 메시지를 수신한다") {
        val room = roomRepository.save(NAME)
        val destination = getDestination(room.id)
        val messageChannel = Channel<System>()
        val sessionToOpenRoom = client.connect(url).withJsonConversions()
        launch {
            sessionToOpenRoom.subscribe(createHeaders(destination, OPEN.name, room.numberOfMember.toString()))
                .take(3)
                .collect { messageChannel.send(getExpectedValue<System>(mapperClient, it.bodyAsText)) }
        }
        val openRoomMessage = messageChannel.receive()
        val enteredMemberId = roomRepository.addMember(room.id)
        val enteredMemberNickname = getNickname(enteredMemberId)
        val sessionToEnterRoom = client.connect(url)
        sessionToEnterRoom.subscribe(createHeaders(destination, ENTER.name, enteredMemberId.toString())).first()
        openRoomMessage.run {
            numberOfMember shouldBe MEMBER_INIT
            text shouldBe (room.name + SystemTextPostfix.OPEN)
        }
        messageChannel.receive().let { enterRoomMessage ->
            enterRoomMessage.numberOfMember shouldBe 2
            enterRoomMessage.text shouldBe (enteredMemberNickname + SystemTextPostfix.ENTER)
        }
        messageChannel.receive().let { exitRoomMessage ->
            exitRoomMessage.numberOfMember shouldBe MEMBER_INIT
            exitRoomMessage.text shouldBe (enteredMemberNickname + SystemTextPostfix.EXIT)
        }
        sessionToEnterRoom.disconnect()
        sessionToOpenRoom.disconnect()
    }

    test("올바르지 않은 목적지로 채팅방을 구독하면 에러 메시지를 수신한다") {
        val room = roomRepository.save(NAME)
        val session = client.connect(url).withJsonConversions()
        getExpectedValue<Error>(
            mapperClient,
            session.subscribe(
                createHeaders(
                    "${WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX}abc",
                    OPEN.name,
                    room.numberOfMember.toString()
                )
            ).first().bodyAsText
        ).code shouldBe ROOM.code
        session.disconnect()
    }

    test("입장한 멤버 정보를 포함하지 않고 채팅방을 구독하면 에러 메시지를 수신한다") {
        val room = roomRepository.save(NAME)
        val session = client.connect(url).withJsonConversions()
        getExpectedValue<Error>(
            mapperClient,
            session.subscribe(createHeaders(getDestination(room.id), OPEN.name, null)).first().bodyAsText
        ).code shouldBe MEMBER.code
        session.disconnect()
    }

    test("생성 또는 입장을 구분하는 타입 정보를 포함하지 않고 채팅방을 구독하면 에러 메시지를 수신한다") {
        val room = roomRepository.save(NAME)
        val session = client.connect(url).withJsonConversions()
        getExpectedValue<Error>(
            mapperClient,
            session.subscribe(createHeaders(getDestination(room.id), null, room.numberOfMember.toString())).first().bodyAsText
        ).code shouldBe TYPE.code
        session.disconnect()
    }

    test("채팅방에 채팅 메시지를 전송하면 모든 멤버들이 수신한다") {
        val room = roomRepository.save(NAME)
        val enteredMemberId = room.numberOfMember.toLong()
        val messageChannel = Channel<String>()
        val session = client.connect(url).withJsonConversions()
        launch {
            session.subscribe(createHeaders(getDestination(room.id), OPEN.name, enteredMemberId.toString()))
                .take(2)
                .collect { messageChannel.send(it.bodyAsText) }
        }
        messageChannel.receive()
        val sender = getNickname(enteredMemberId)
        val text = "안녕하세요!"
        session.convertAndSend(
            WebSocketConfig.SEND_DESTINATION_PREFIX + room.id,
            TestChatMessage(sender, text),
            TestChatMessage.serializer()
        )
        getExpectedValue<Chat>(mapperClient, messageChannel.receive()).let {
            it.sender shouldBe sender
            it.text shouldBe text
        }
        session.disconnect()
    }

    afterRootTest {
        roomRepository.deleteAll()
        memberRepository.deleteAll()
    }
}) {
    @Serializable
    private data class TestChatMessage(
        val sender: String,
        val text: String
    )
}