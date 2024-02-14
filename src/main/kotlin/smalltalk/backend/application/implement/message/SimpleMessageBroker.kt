package smalltalk.backend.application.implement.message

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

@Component
class SimpleMessageBroker(
    private val template: SimpMessagingTemplate
): MessageBroker {

    override fun send(topic: String, message: Any) {
        template.convertAndSend(topic, message);
    }
}