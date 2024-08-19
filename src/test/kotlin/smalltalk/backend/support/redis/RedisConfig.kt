package smalltalk.backend.support.redis

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer

@TestConfiguration
class RedisConfig {
    @Value("\${spring.data.redis.host}")
    private lateinit var host: String
    @Value("\${spring.data.redis.port}")
    private lateinit var port: String

    @Bean
    fun redisConnectionFactory() = LettuceConnectionFactory(host, port.toInt())

    @Bean
    fun redisTemplate(redisConnectionFactory: LettuceConnectionFactory) =
        RedisTemplate<String, Any>().apply {
            keySerializer = StringRedisSerializer()
            valueSerializer = GenericJackson2JsonRedisSerializer()
            connectionFactory = redisConnectionFactory
        }
}