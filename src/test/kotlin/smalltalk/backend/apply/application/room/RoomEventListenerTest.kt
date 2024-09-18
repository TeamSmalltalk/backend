package smalltalk.backend.apply.application.room

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.simp.stomp.StompHeaders
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import smalltalk.backend.application.room.RoomEventListener
import smalltalk.backend.apply.NAME
import smalltalk.backend.config.websocket.WebSocketConfig
import smalltalk.backend.infra.repository.room.RoomRepository
import smalltalk.backend.presentation.dto.message.Bot
import smalltalk.backend.presentation.dto.message.BotText
import smalltalk.backend.support.redis.RedisContainerConfig
import smalltalk.backend.support.spec.afterRootTest
import smalltalk.backend.support.websocket.TestHandler
import java.util.concurrent.CompletableFuture


@ActiveProfiles("test")
@Import(RedisContainerConfig::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
class RoomEventListenerTest(
    @LocalServerPort
    private val port: Int,
    private val roomRepository: RoomRepository,
    private val mapper: ObjectMapper
) : FunSpec({
    val logger = KotlinLogging.logger { }
    val url = "ws://localhost:$port${WebSocketConfig.STOMP_ENDPOINT}"
    val stompClient = WebSocketStompClient(StandardWebSocketClient()).apply {
        messageConverter = MappingJackson2MessageConverter(mapper)
    }
    var stompSession = CompletableFuture<StompSession>()

    test("채팅방을 생성하면 메시지를 수신해야 한다") {
        // Given
        val roomId = roomRepository.save(NAME).id
        val destination = WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX + roomId
        val handler = TestHandler(destination, Bot::class.java)
        stompSession = stompClient.connectAsync(url, handler)

        // When
        val messages = handler.awaitMessage()

        // Then
        val room = roomRepository.getById(roomId)
        messages.last().run {
            room.let {
                shouldNotBeNull()
                numberOfMember shouldBe it.members.size
                text shouldBe (it.name + BotText.OPEN)
            }
        }
    }

    test("채팅방에 입장하면 메시지를 수신해야 한다") {
        // Given
        val roomId = roomRepository.save(NAME).id
        val destination = WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX + roomId
        val handler = TestHandler(destination, Bot::class.java)
        roomRepository.addMember(roomId)
        stompSession = stompClient.connectAsync(url, handler)

        // When
        val messages = handler.awaitMessage()

        // Then
        val members = roomRepository.getById(roomId).members
        messages.last().run {
            members.let {
                shouldNotBeNull()
                numberOfMember shouldBe it.size
                text shouldBe (RoomEventListener.NICKNAME_PREFIX + it.last() + BotText.ENTRANCE)
            }
        }
    }

    test("채팅방에서 퇴장하면 이를 제외한 멤버들은 메시지를 수신해야 한다") {
        // Given
        val roomId = roomRepository.save(NAME).id
        val destination = WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX + roomId
        val memberIdToDelete = 2L
        val handler = TestHandler(destination, Bot::class.java)
        val handlerToDisconnect = handler.copy()
        stompSession = stompClient.connectAsync(url, handler)
        handler.awaitMessage()
        roomRepository.addMember(roomId)
        val stompSessionToDisconnect = stompClient.connectAsync(url, handlerToDisconnect)
        handler.awaitMessage()
        handlerToDisconnect.awaitMessage()

        // When
        handlerToDisconnect.unsubscribe(
            StompHeaders().apply {
                this.destination = destination
                add(RoomEventListener.MEMBER_ID_HEADER, memberIdToDelete.toString())
            }
        )
        val messages = handler.awaitMessage()

        // Then
        messages.run {
            roomRepository.getById(roomId).let {
                shouldHaveSize(3)
                last().numberOfMember shouldBe 1
                last().text shouldBe (RoomEventListener.NICKNAME_PREFIX + memberIdToDelete + BotText.EXIT)
                it.members shouldNotContain memberIdToDelete
                it.idQueue shouldContain memberIdToDelete
            }
        }
        stompSessionToDisconnect.get().disconnect()
    }

    afterRootTest {
        roomRepository.deleteAll()
        stompSession.get().disconnect()
        stompClient.stop()
    }
})