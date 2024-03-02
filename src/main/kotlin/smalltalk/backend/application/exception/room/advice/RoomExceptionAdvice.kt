package smalltalk.backend.application.exception.room.advice

import org.springframework.http.HttpStatus.*
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import smalltalk.backend.application.exception.room.situation.GeneratingRoomIdFailureException
import smalltalk.backend.application.exception.room.situation.RoomNotFoundException


@RestControllerAdvice
class RoomExceptionAdvice {
    @ResponseStatus(value = BAD_REQUEST, reason = "Could not generate room id")
    @ExceptionHandler(GeneratingRoomIdFailureException::class)
    fun generatingRoomIdFailureException() {}

    @ResponseStatus(value = NOT_FOUND, reason = "Room could not be found")
    @ExceptionHandler(RoomNotFoundException::class)
    fun roomNotFoundException() {}
}