package smalltalk.backend.apply.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.FunSpec
import io.mockk.mockk
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.web.socket.messaging.WebSocketStompClient
import smalltalk.backend.presentation.dto.message.Message

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StompMessagingTest(
    val client: WebSocketStompClient,
    @LocalServerPort
    val port: Int
) : FunSpec({
    val logger = KotlinLogging.logger { }
    val handler = mockk<StompSessionHandlerAdapter>(relaxed = true)
    val webSocketUrl = "ws://localhost:$port/ws-connect"
    val destinationToSubscribe = "/rooms/1"
    val destinationToSend = "/rooms/chat/1"

    test("메시지를 전송한다") {
        val session = client.connectAsync(webSocketUrl, handler).join()
        session.subscribe(destinationToSubscribe, handler)
        session.send(
            destinationToSend,
            Message(
                "관리자",
                "입장"
            )
        )
        Thread.sleep(500)
        session.disconnect()
        client.stop()
    }
})