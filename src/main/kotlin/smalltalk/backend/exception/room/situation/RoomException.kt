package smalltalk.backend.exception.room.situation

const val DOESNT_EXIST_HEADER_MESSAGE_PREFIX = "Doesnt exist "

class RoomIdNotGeneratedException : RuntimeException()
class RoomNotFoundException : RuntimeException()
class FullRoomException : RuntimeException()
class DoesntExistHeaderException(name: String) : RuntimeException(DOESNT_EXIST_HEADER_MESSAGE_PREFIX + name)
class MemberNotFoundException() : RuntimeException()