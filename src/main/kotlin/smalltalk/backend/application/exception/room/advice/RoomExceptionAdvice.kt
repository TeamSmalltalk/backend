package smalltalk.backend.application.exception.room.advice

import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import smalltalk.backend.application.exception.room.situation.RoomIdNotFoundException
import smalltalk.backend.application.exception.room.situation.RoomNotFoundException


@RestControllerAdvice
class RoomExceptionAdvice {
    @ResponseStatus(value = NOT_FOUND, reason = "Room id could not be found")
    @ExceptionHandler(RoomIdNotFoundException::class)
    fun roomIdNotFoundException() {}

    @ResponseStatus(value = NOT_FOUND, reason = "Room could not be found")
    @ExceptionHandler(RoomNotFoundException::class)
    fun roomNotFoundException() {}
}