package smalltalk.backend.apply.application.websocket

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.FunSpec
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
import java.util.concurrent.CountDownLatch


@ActiveProfiles("test")
@Import(RedisContainerConfig::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
class WebSocketEventListenerTest(
    @LocalServerPort
    private val port: Int,
    private val roomRepository: RoomRepository,
    private val mapper: ObjectMapper
) : FunSpec({
    val logger = KotlinLogging.logger { }
    val url = "ws://localhost:$port${WebSocketConfig.STOMP_ENDPOINT}"
    val stompClient = WebSocketStompClient(StandardWebSocketClient())

    test("사용자가 채팅방을 구독하면 메시지를 수신해야 한다") {
        // Given
        val roomId = roomRepository.save(NAME).id
        val handler = TestHandler(WebSocketConfig.SUBSCRIBE_ROOM_DESTINATION_PREFIX + roomId)
        val stompSession = stompClient.apply {
            messageConverter = MappingJackson2MessageConverter(mapper)
        }.connectAsync(url, handler)

        // When
        val message = handler.awaitMessage()

        // Then
        logger.info { message }
        stompSession.get().disconnect()
        stompClient.stop()
    }
}) {
    private class TestHandler(
        private val destination: String
    ) : StompSessionHandlerAdapter() {
        private val logger = KotlinLogging.logger { }
        private var message: Entrance? = null
        private val receiver = CountDownLatch(1)

        override fun afterConnected(session: StompSession, connectedHeaders: StompHeaders) {
            session.subscribe(destination, object : StompFrameHandler {
                override fun getPayloadType(headers: StompHeaders) =
                    Entrance::class.java

                override fun handleFrame(headers: StompHeaders, payload: Any?) {
                    message = payload as? Entrance
                    receiver.countDown()
                }
            })
        }

        fun awaitMessage(): Entrance? {
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