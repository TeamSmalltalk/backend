package smalltalk.backend.config.redis.sub

import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import smalltalk.backend.logger

class RedisSubscriber : MessageListener {
    lateinit var message: Message

    override fun onMessage(message: Message, pattern: ByteArray?) {
        logger.info { message }
        this.message = message
    }
}