package smalltalk.backend.infrastructure.broker

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component


@Component
class SimpleMessageBroker(
    private val template: SimpMessagingTemplate
): MessageBroker {
    override fun send(topic: String, message: Any) {
        template.convertAndSend(topic, message)
    }
}