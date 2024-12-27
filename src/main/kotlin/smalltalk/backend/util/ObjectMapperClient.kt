package smalltalk.backend.util

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class ObjectMapperClient(private val objectMapper: ObjectMapper) {
    fun getStringValue(value: Any): String = objectMapper.writeValueAsString(value)

    fun getByteArrayValue(value: Any): ByteArray = objectMapper.writeValueAsBytes(value)

    fun <T> getExpectedValue(value: Any, expectedType: Class<T>): T =
        when (value) {
            is String -> objectMapper.readValue(value, expectedType)
            is ByteArray -> objectMapper.readValue(value, expectedType)
            else -> throw IllegalStateException("Not allowed type")
        }
}