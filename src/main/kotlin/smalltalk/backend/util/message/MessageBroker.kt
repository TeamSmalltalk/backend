package smalltalk.backend.util.message


interface MessageBroker {
    fun send(topic: String, message: Any)
}