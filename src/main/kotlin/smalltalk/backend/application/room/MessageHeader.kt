package smalltalk.backend.application.room

enum class MessageHeader(val key: String, val code: String) {
    SESSION("sessionId", "600"),
    SUBSCRIPTION("subscriptionId", "600"),
    ROOM("roomId", "601"),
    MEMBER("memberId", "602"),
    TYPE("type", "603")
}