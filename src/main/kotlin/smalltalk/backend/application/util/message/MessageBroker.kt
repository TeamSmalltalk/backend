package smalltalk.backend.application.util.message


interface MessageBroker {
    fun send(topic: String, message: Any)
}