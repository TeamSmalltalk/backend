package smalltalk.backend.apply.websocket

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.*
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.messaging.simp.stomp.StompSession
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.socket.messaging.WebSocketStompClient

@ActiveProfiles("test")
@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StompConnectionTests(
    private val client: WebSocketStompClient,
    @LocalServerPort private val port: Int = 0
) : FunSpec({

    val logger = KotlinLogging.logger {}
    val webSocketUrl = "ws://localhost:$port/ws-connect"

    test("비동기로 20개의 Stomp 클라이언트 연결이 한번에 생성됐다면 id 가 다른 20개의 세션이 있어야 한다.") {
        coroutineScope {
            val sessionIds = hashSetOf<String>()
            val sessions = mutableListOf<StompSession>()
            val jobs = mutableListOf<Job>()
            repeat(20) {
                jobs.add(
                    launch {
                        val stompSession = client
                            .connectAsync(webSocketUrl, mockk<StompSessionHandlerAdapter>(relaxed = true))
                            .join()
                        sessions.add(stompSession)
                        sessionIds.add(stompSession.sessionId)
                    }
                )
            }
            jobs.joinAll()
            sessionIds.size shouldBe 20
            sessions.forEach { it.disconnect() }
            client.stop()
        }
    }

    test("동기로 20개의 Stomp 클라이언트 연결이 한번에 생성됐다면 id 가 다른 20개의 세션이 있어야 한다") {
        val sessionIds = hashSetOf<String>()
        val sessions = mutableListOf<StompSession>()
        repeat(20) {
            val stompSession = client
                .connectAsync(webSocketUrl, mockk<StompSessionHandlerAdapter>(relaxed = true))
                .join()
            sessions.add(stompSession)
            sessionIds.add(stompSession.sessionId)
        }
        sessionIds.size shouldBe 20
        sessions.forEach { it.disconnect() }
        client.stop()
    }

})