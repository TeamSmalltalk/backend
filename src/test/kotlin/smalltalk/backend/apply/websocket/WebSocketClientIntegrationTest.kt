package smalltalk.backend.apply.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
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
class WebSocketClientIntegrationTest(
    @LocalServerPort
    private val port: Int,
    private val roomRepository: RoomRepository,
    private val memberRepository: MemberRepository
) : FunSpec({
    val logger = KotlinLogging.logger { }
    val url = "ws://localhost:$port${WebSocketConfig.STOMP_ENDPOINT}"
    val client = StompClient(StandardWebSocketClient().asKrossbowWebSocketClient())

    test("채팅방을 생성하면 메시지를 수신해야 한다") {
        // Given
        val room = roomRepository.save(NAME)
        val roomId = room.id
        val destination = WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX + roomId
        val session = client.connect(url).withJsonConversions()

        // When
        val message = session.subscribe(createHeaders(destination, OPEN.name, room.members.last().toString()), Bot.serializer()).first()

        // Then
        message.run {
            roomRepository.getById(roomId).let { room ->
                numberOfMember shouldBe room.members.size
                text shouldBe (room.name + BotText.OPEN)
            }
        }
        memberRepository.findAll() shouldHaveSize 1
        session.disconnect()
    }

    test("채팅방에 입장하면 모든 멤버가 메시지를 수신해야 한다") {
        // Given
        val roomId = roomRepository.save(NAME).id
        val destination = WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX + roomId
        val enteredMemberId = roomRepository.addMember(roomId)

        // When
        val session = client.connect(url).withJsonConversions()
        val message = session.subscribe(createHeaders(destination, ENTER.name, enteredMemberId.toString()), Bot.serializer()).first()

        // Then
        message.run {
            numberOfMember shouldBe roomRepository.getById(roomId).members.size
            text shouldBe (RoomEventListener.NICKNAME_PREFIX + enteredMemberId + BotText.ENTRANCE)
        }
        memberRepository.findAll() shouldHaveSize 1
        session.disconnect()
    }

    afterRootTest {
        roomRepository.deleteAll()
        memberRepository.deleteAll()
    }
})