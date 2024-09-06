package smalltalk.backend.apply.application.room

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.messaging.converter.MappingJackson2MessageConverter
import org.springframework.messaging.simp.stomp.*
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.messaging.WebSocketStompClient
import smalltalk.backend.apply.NAME
import smalltalk.backend.config.websocket.WebSocketConfig
import smalltalk.backend.infra.repository.room.RoomRepository
import smalltalk.backend.presentation.dto.message.Entrance
import smalltalk.backend.support.redis.RedisContainerConfig
import smalltalk.backend.support.spec.afterRootTest
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch


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
        val handler = TestHandler(
            WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX + roomId,
            Entrance::class.java
            )
        stompSession = stompClient.connectAsync(url, handler)

        // When
        val message = handler.awaitMessage()

        // Then
        val members = roomRepository.getById(roomId).members
        message.run {
            members.let {
                shouldNotBeNull()
                numberOfMember shouldBe it.size
                nickname shouldBe "익명${it.last()}"
            }
        }
    }

    afterRootTest {
        roomRepository.deleteAll()
        stompSession.get().disconnect()
        stompClient.stop()
    }
}) {
    private class TestHandler<T>(
        private val destination: String,
        private val payloadType: Class<T>
    ) : StompSessionHandlerAdapter() {
        private val logger = KotlinLogging.logger { }
        private var message: T? = null
        private val receiver = CountDownLatch(1)

        override fun afterConnected(session: StompSession, connectedHeaders: StompHeaders) {
            session.subscribe(destination, object : StompFrameHandler {
                override fun getPayloadType(headers: StompHeaders) = this@TestHandler.payloadType

                override fun handleFrame(headers: StompHeaders, payload: Any?) {
                    message = payloadType.cast(payload)
                    receiver.countDown()
                }
            })
        }

        fun awaitMessage(): T? {
            receiver.await()
            return message
        }

        override fun handleFrame(headers: StompHeaders, payload: Any?) {
        }

        override fun handleException(
            session: StompSession,
            command: StompCommand?,
            headers: StompHeaders,
            payload: ByteArray,
            exception: Throwable
        ) {
            logger.error { exception }
        }

        override fun handleTransportError(session: StompSession, exception: Throwable) {
            logger.error { exception }
        }
    }
}