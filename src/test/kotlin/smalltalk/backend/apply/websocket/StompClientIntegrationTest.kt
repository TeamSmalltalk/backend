package smalltalk.backend.apply.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import org.hildan.krossbow.stomp.StompClient
import org.hildan.krossbow.stomp.conversions.kxserialization.json.withJsonConversions
import org.hildan.krossbow.websocket.spring.asKrossbowWebSocketClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import smalltalk.backend.application.room.RoomEventListener
import smalltalk.backend.application.room.Type.ENTER
import smalltalk.backend.application.room.Type.OPEN
import smalltalk.backend.apply.NAME
import smalltalk.backend.apply.createHeaders
import smalltalk.backend.config.websocket.WebSocketConfig
import smalltalk.backend.infra.repository.member.MemberRepository
import smalltalk.backend.infra.repository.room.RoomRepository
import smalltalk.backend.presentation.dto.message.BotText
import smalltalk.backend.support.redis.RedisContainerConfig
import smalltalk.backend.support.spec.afterRootTest

@ActiveProfiles("test")
@Import(RedisContainerConfig::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
class StompClientIntegrationTest(
    @LocalServerPort
    private val port: Int,
    private val roomRepository: RoomRepository,
    private val memberRepository: MemberRepository
) : FunSpec({
    val logger = KotlinLogging.logger { }
    val url = "ws://localhost:$port${WebSocketConfig.STOMP_ENDPOINT}"
    val client = StompClient(StandardWebSocketClient().asKrossbowWebSocketClient())

    test("채팅방을 입장 및 퇴장하면 메시지를 수신해야 한다") {
        // Given
        val messageChannel = Channel<Bot>()
        val room = roomRepository.save(NAME)
        val roomId = room.id
        val destination = WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX + roomId
        val session = client.connect(url).withJsonConversions()
        launch {
            session.subscribe(createHeaders(destination, OPEN.name, room.members.last().toString()), Bot.serializer())
                .take(3)
                .collect { messageChannel.send(it) }
        }
        val receivedMessageWhenOpenRoom = messageChannel.receive()
        val memberIdToDelete = roomRepository.addMember(roomId)

        // When
        val receivedMessagesWhenEnterAndExitRoom = mutableListOf<Bot>()
        val sessionToReceiveExitMessage = client.connect(url).withJsonConversions()
        sessionToReceiveExitMessage.subscribe(createHeaders(destination, ENTER.name, memberIdToDelete.toString()), Bot.serializer()).first()
        repeat(2) {
            receivedMessagesWhenEnterAndExitRoom.add(messageChannel.receive())
        }

        // Then
        val latestNumberOfMember = room.members.size
        receivedMessageWhenOpenRoom.run {
            numberOfMember shouldBe latestNumberOfMember
            text shouldBe (room.name + BotText.OPEN)
        }
        receivedMessagesWhenEnterAndExitRoom.run {
            first().run {
                numberOfMember shouldBe (latestNumberOfMember + 1)
                text shouldBe (RoomEventListener.NICKNAME_PREFIX + memberIdToDelete + BotText.ENTRANCE)
            }
            last().run {
                numberOfMember shouldBe latestNumberOfMember
                text shouldBe (RoomEventListener.NICKNAME_PREFIX + memberIdToDelete + BotText.EXIT)
            }
        }
        sessionToReceiveExitMessage.disconnect()
        session.disconnect()
    }

    afterRootTest {
        roomRepository.deleteAll()
        memberRepository.deleteAll()
    }
})