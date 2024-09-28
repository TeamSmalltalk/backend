package smalltalk.backend.exception.room.advice

import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.NOT_FOUND
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import smalltalk.backend.exception.room.situation.RoomIdNotGeneratedException
import smalltalk.backend.exception.room.situation.RoomNotFoundException
import smalltalk.backend.presentation.dto.message.Error


@RestControllerAdvice
class RoomExceptionAdvice {
    @ResponseStatus(value = INTERNAL_SERVER_ERROR, reason = "Could not generate room id")
    @ExceptionHandler(RoomIdNotGeneratedException::class)
    fun roomIdNotGeneratedException() = Error(RoomExceptionSituationCode.COMMON.code)

    @ResponseStatus(value = NOT_FOUND, reason = "Room could not be found")
    @ExceptionHandler(RoomNotFoundException::class)
    fun roomNotFoundException() {}
}