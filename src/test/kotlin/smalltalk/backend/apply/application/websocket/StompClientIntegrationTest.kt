package smalltalk.backend.apply.application.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import org.hildan.krossbow.stomp.StompClient
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
import smalltalk.backend.apply.NAME
import smalltalk.backend.apply.createHeaders
import smalltalk.backend.config.websocket.WebSocketConfig
import smalltalk.backend.infra.repository.member.MemberRepository
import smalltalk.backend.infra.repository.room.RoomRepository
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
) : FunSpec({
    val logger = KotlinLogging.logger { }
    val url = "ws://localhost:$port${WebSocketConfig.STOMP_ENDPOINT}"
    val client = StompClient(StandardWebSocketClient().asKrossbowWebSocketClient())

    test("채팅방을 입장 및 퇴장하면 메시지를 수신해야 한다") {
        // Given
        val messageChannel = Channel<System>()
        val room = roomRepository.save(NAME)
        val roomId = room.id
        val destination = WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX + roomId
        val session = client.connect(url)
        launch {
            session.subscribe(createHeaders(destination, OPEN.name, room.members.last().toString()))
                .take(3)
                .collect { messageChannel.send(mapperClient.getExpectedValue(it.bodyAsText, System::class.java)) }
        }
        val receivedMessageWhenOpenRoom = messageChannel.receive()
        val memberIdToDelete = roomRepository.addMember(roomId)

        // When
        val receivedMessagesWhenEnterAndExitRoom = mutableListOf<System>()
        val sessionToReceiveExitMessage = client.connect(url)
        sessionToReceiveExitMessage.subscribe(createHeaders(destination, ENTER.name, memberIdToDelete.toString())).first()
        repeat(2) {
            receivedMessagesWhenEnterAndExitRoom.add(messageChannel.receive())
        }

        // Then
        val latestNumberOfMember = room.members.size
        receivedMessageWhenOpenRoom.let { openMessage ->
            openMessage.numberOfMember shouldBe latestNumberOfMember
            openMessage.text shouldBe (room.name + SystemTextPostfix.OPEN)
        }
        receivedMessagesWhenEnterAndExitRoom.run {
            first().let { enterMessage ->
                enterMessage.numberOfMember shouldBe (latestNumberOfMember + 1)
                enterMessage.text shouldBe ("익명" + memberIdToDelete + SystemTextPostfix.ENTRANCE)
            }
            last().let { exitMessage ->
                exitMessage.numberOfMember shouldBe latestNumberOfMember
                exitMessage.text shouldBe ("익명" + memberIdToDelete + SystemTextPostfix.EXIT)
            }
        }
        sessionToReceiveExitMessage.disconnect()
        session.disconnect()
    }

    test("채팅방 입장 시 예외가 발생하면 메시지를 수신해야 한다") {
        // Given
        val room = roomRepository.save(NAME)
        val roomId = room.id
        val invalidDestination = "${WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX}abc"
        val enteredMemberId = roomRepository.addMember(roomId)

        // When
        val session = client.connect(url)
        launch {
            session.subscribe(createHeaders(invalidDestination, ENTER.name, enteredMemberId.toString()))
                .collect {
                    when (mapperClient.getExpectedValue(it.bodyAsText, Error::class.java).code) {
                        ROOM.code -> {
                            cancel()
                        }
                    }
                }
        }.join()

        // Then
        session.disconnect()
    }

    afterRootTest {
        roomRepository.deleteAll()
        memberRepository.deleteAll()
    }
})