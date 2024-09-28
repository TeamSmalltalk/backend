package smalltalk.backend.exception.room.advice

import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import smalltalk.backend.exception.room.advice.RoomExceptionSituationCode.*
import smalltalk.backend.exception.room.situation.FullRoomException
import smalltalk.backend.exception.room.situation.RoomIdNotGeneratedException
import smalltalk.backend.exception.room.situation.RoomNotFoundException
import smalltalk.backend.presentation.dto.message.Error


@RestControllerAdvice
class RoomExceptionAdvice {
    @ExceptionHandler(RoomIdNotGeneratedException::class)
    fun roomIdNotGeneratedException() = ResponseEntity.status(INTERNAL_SERVER_ERROR).body(Error(COMMON.code))

    @ExceptionHandler(RoomNotFoundException::class)
    fun roomNotFoundException() = ResponseEntity.status(NOT_FOUND).body(Error(DELETED.code))

    @ExceptionHandler(FullRoomException::class)
    fun fullRoomException() = ResponseEntity.status(BAD_REQUEST).body(Error(FULL.code))
}