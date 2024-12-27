package smalltalk.backend.application


interface MessageBroker {
    fun send(topic: String, message: Any)
}