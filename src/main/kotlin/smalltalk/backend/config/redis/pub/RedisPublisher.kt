package smalltalk.backend.config.redis.pub

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisPublisher(
    private val template: StringRedisTemplate
) : MessagePublisher {
    override fun publish(topic: String, message: Any) {
        template.convertAndSend(topic, message)
    }
}