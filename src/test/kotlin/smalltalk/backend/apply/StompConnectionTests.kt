package smalltalk.backend.apply

import io.github.oshai.kotlinlogging.KotlinLogging
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.web.socket.messaging.WebSocketStompClient
import smalltalk.backend.config.websocket.WebSocketConfig
import smalltalk.backend.presentation.dto.message.Message



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(MockitoExtension::class)
class StompConnectionTests (
    @Autowired private val client: WebSocketStompClient,
    @Mock private val mockSessionHandler: StompSessionHandlerAdapter
) {

    @LocalServerPort
    private val port = 0;
    private val roomId = 1;
    private lateinit var stompSession: StompSession
    private val logger = KotlinLogging.logger {  }

    @BeforeEach
    fun setUp() {
        stompSession = client
            .connectAsync("ws://localhost:$port/ws-connect", mockSessionHandler)
            .join()
    }

    @Test
    fun testSendMessage() {
        stompSession.subscribe(WebSocketConfig.SUBSCRIBE_DESTINATION_PREFIX + roomId, mockSessionHandler)
        stompSession.send(WebSocketConfig.SEND_DESTINATION_PREFIX + roomId, Message("wee", "hi everyone"))
    }

    @AfterEach
    fun tearDown() {
        logger.info { "pre stop" }
        client.stop()
        logger.info { "post stop" }
        Thread.sleep(2000)
        logger.info { "pre disconnect" }
        stompSession.disconnect()
        logger.info { "post disconnect" }
    }
}