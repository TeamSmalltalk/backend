package smalltalk.backend.application.message

import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component


@Component
class SimpleMessageBroker(
    private val template: SimpMessagingTemplate
): MessageBroker {
    override fun send(topic: String, message: Any, headers: Map<String, Any>?) {
        template.convertAndSend(topic, message, headers)
    }
}