package smalltalk.backend.config.redis

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig(
    @Value("\${spring.redis.host}")
    private val host: String,
    @Value("\${spring.redis.port}")
    private val port: Int
) {

    @Bean
    fun objectMapper() = ObjectMapper()

    @Bean
    fun redisConnectionFactory() = LettuceConnectionFactory(host, port)

    @Bean
    fun redisTemplate() =
        RedisTemplate<String, Any>().apply {
            keySerializer = StringRedisSerializer()
            valueSerializer = StringRedisSerializer()
            connectionFactory = redisConnectionFactory()
        }
}