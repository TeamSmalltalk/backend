package smalltalk.backend.config.redis.pub

interface MessagePublisher {
    fun publish(topic: String, message: Any)
}