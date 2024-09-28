package smalltalk.backend.exception.room.advice

enum class RoomExceptionSituationCode(val code: String) {
    COMMON("600"),
    NOT_FOUND("601"),
    FULL("602")
}