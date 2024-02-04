package smalltalk.backend.application.implement.message


interface MessageBroker {
    fun send(topic: String, message: Any)
}