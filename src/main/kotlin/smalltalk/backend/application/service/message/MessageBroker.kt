package smalltalk.backend.application.service.message


interface MessageBroker {
    fun send(topic: String, message: Any)
}