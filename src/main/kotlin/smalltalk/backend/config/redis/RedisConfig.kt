package smalltalk.backend.config.redis

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig(
    @Value("\${spring.data.redis.host}")
    private val host: String,
    @Value("\${spring.data.redis.port}")
    private val port: Int
) {

    @Bean
    fun redisConnectionFactory() = LettuceConnectionFactory(host, port)

    @Bean
    fun redisTemplate(): RedisTemplate<String, String> {

        val redisTemplate: RedisTemplate<String, String> = RedisTemplate()
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = StringRedisSerializer()
        redisTemplate.connectionFactory = redisConnectionFactory()

        return redisTemplate
    }
}