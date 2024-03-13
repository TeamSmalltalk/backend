package smalltalk.backend.apply

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.common.runBlocking
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
import smalltalk.backend.config.websocket.WebSocketConfig
import java.time.LocalDateTime

@ActiveProfiles("test")
@DirtiesContext
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StompMultiThreadTests(
    private val client: WebSocketStompClient,
    @LocalServerPort private val port: Int = 0
) : FunSpec({

    val logger = KotlinLogging.logger{}
    val webSocketUrl = "ws://localhost:$port/ws-connect"

    test("20개의 Stomp 클라이언트 연결이 한번에 생성됐다면 webSocketSessionStorage 에 id 가 다른 20개의 세션이 있어야") {
        coroutineScope {
            val sessionSet = hashSetOf<String>()
            launch {
                repeat(20) {
                    sessionSet.add(
                        client
                            .connectAsync(webSocketUrl, mockk<StompSessionHandlerAdapter>(relaxed = true))
                            .join()
                            .sessionId
                    )
                }
            }.join()
            sessionSet.size shouldBe 20
        }
    }

//    test("20개의 Stomp 클라이언트 연결이 한번에 생성됐다면 webSocketSessionStorage 에 id 가 다른 20개의 세션이 있어야 한다. 쌩") {
//        val sessionSet = hashSetOf<String>()
//        logger.info { "1 main" }
//        repeat(300) {
//            sessionSet.add(
//                client
//                    .connectAsync(webSocketUrl, mockk<StompSessionHandlerAdapter>(relaxed = true))
//                    .join()
//                    .sessionId
//            )
//        }
//        logger.info { "4 main" }
//        sessionSet.size shouldBe 300
//    }
//
//
//
//    test("코루틴 테스트") {
//        coroutineScope { // this: Co
//            launch {
//                delay(5000L)
//                logger.info { "Hello" }
//            }
//            logger.info { "World!" }
//        }
//    }


})