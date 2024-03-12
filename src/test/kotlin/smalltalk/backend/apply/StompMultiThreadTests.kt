package smalltalk.backend.apply

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.common.runBlocking
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.socket.messaging.WebSocketStompClient
import smalltalk.backend.config.websocket.WebSocketConfig

@ActiveProfiles("test")
@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StompMultiThreadTests(
    private val client: WebSocketStompClient,
    @LocalServerPort private val port: Int = 0
) : FunSpec({

    val logger = KotlinLogging.logger{}
    val webSocketUrl = "ws://localhost:$port/ws-connect"

    test("20개의 Stomp 클라이언트 연결이 한번에 생성됐다면 webSocketSessionStorage 에 id 가 다른 20개의 세션이 있어야 한다.") {
        runBlocking {
            val sessionSet = hashSetOf<String>()
            val jobs = List(20) {
                launch (Dispatchers.IO) {
                    logger.info { "koroutine $it" }
                    sessionSet.add(
                        client
                            .connectAsync(webSocketUrl, mockk<StompSessionHandlerAdapter>(relaxed = true))
                            .join()
                            .sessionId
                    )
                }
            }
            jobs.joinAll()
            sessionSet.size shouldBe 20
        }
    }
})