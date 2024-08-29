package smalltalk.backend.config.redis

interface MessageDelegate {
    fun handleMessage(message: String)
    fun handleMessage(message: ByteArray)
}