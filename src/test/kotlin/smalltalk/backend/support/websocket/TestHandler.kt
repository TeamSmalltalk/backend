//package smalltalk.backend.support.websocket
//
//import io.github.oshai.kotlinlogging.KotlinLogging
//import org.springframework.messaging.simp.stomp.StompFrameHandler
//import org.springframework.messaging.simp.stomp.StompHeaders
//import org.springframework.messaging.simp.stomp.StompSession
//import org.springframework.messaging.simp.stomp.StompSession.Subscription
//import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter
//import java.lang.reflect.Type
//import java.util.concurrent.CountDownLatch
//
//class TestHandler<T>(
//    private val destination: String,
//    private val payloadType: Class<T>
//) : StompSessionHandlerAdapter() {
//    private val logger = KotlinLogging.logger { }
//    private var messages = mutableListOf<T>()
//    private var receiver = CountDownLatch(1)
//    private lateinit var subscription: Subscription
//
//    override fun afterConnected(session: StompSession, connectedHeaders: StompHeaders) {
//        subscription =
//            session.subscribe(destination, object : StompFrameHandler {
//                override fun getPayloadType(headers: StompHeaders): Type {
//                    return payloadType
//                }
//
//                override fun handleFrame(headers: StompHeaders, payload: Any?) {
//                    logger.info { payload }
//                    messages.add(payloadType.cast(payload))
//                    receiver.countDown()
//                }
//            })
//    }
//
//    fun unsubscribe(headers: StompHeaders) = subscription.unsubscribe(headers)
//
//    fun awaitMessage(): MutableList<T> {
//        receiver.await()
//        resetReceiver()
//        return messages
//    }
//
//    fun copy() = TestHandler(destination, payloadType)
//
//    private fun resetReceiver() {
//        receiver = CountDownLatch(1)
//    }
//}