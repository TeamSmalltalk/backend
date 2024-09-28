package smalltalk.backend.application.service.room

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import smalltalk.backend.util.message.MessageBroker


@Service
class RoomService(private val messageBroker: MessageBroker) {
    private val logger = KotlinLogging.logger { }


}