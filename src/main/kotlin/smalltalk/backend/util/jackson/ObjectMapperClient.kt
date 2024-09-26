package smalltalk.backend.util.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class ObjectMapperClient(private val mapper: ObjectMapper) {
    fun getStringValue(value: Any): String = mapper.writeValueAsString(value)

    fun getByteArrayValue(value: Any): ByteArray = mapper.writeValueAsBytes(value)

    fun <T> getExpectedValue(value: Any, expectedType: Class<T>): T =
        when (value) {
            is String -> mapper.readValue(value, expectedType)
            is ByteArray -> mapper.readValue(value, expectedType)
            else -> throw IllegalStateException("Not allowed type")
        }
}