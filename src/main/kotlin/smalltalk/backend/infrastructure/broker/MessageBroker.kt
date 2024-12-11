package smalltalk.backend.infrastructure.broker


interface MessageBroker {
    fun send(topic: String, message: Any)
}