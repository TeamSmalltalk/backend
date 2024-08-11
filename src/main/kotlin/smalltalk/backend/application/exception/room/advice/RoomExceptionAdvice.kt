package smalltalk.backend.application.exception.room.advice

import org.springframework.http.HttpStatus.*
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import smalltalk.backend.application.exception.room.situation.FullRoomException
import smalltalk.backend.application.exception.room.situation.RoomIdNotGeneratedException
import smalltalk.backend.application.exception.room.situation.RoomNotFoundException


@RestControllerAdvice
class RoomExceptionAdvice {
    @ResponseStatus(value = INTERNAL_SERVER_ERROR, reason = "Could not generate room id")
    @ExceptionHandler(RoomIdNotGeneratedException::class)
    fun roomIdNotGeneratedException() {}

    @ResponseStatus(value = NOT_FOUND, reason = "Room could not be found")
    @ExceptionHandler(RoomNotFoundException::class)
    fun roomNotFoundException() {}

    @ResponseStatus(value = BAD_REQUEST, reason = "Room is full")
    @ExceptionHandler(FullRoomException::class)
    fun fullRoomException() {}
}