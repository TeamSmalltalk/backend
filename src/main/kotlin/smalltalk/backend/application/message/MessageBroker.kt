package smalltalk.backend.application.message


interface MessageBroker {
    fun send(topic: String, message: Any, headers: Map<String, Any>?)
}