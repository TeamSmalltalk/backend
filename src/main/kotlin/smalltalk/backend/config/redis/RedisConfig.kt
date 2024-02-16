package smalltalk.backend.config.redis

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig {

    @Bean
    fun redisTemplate(redisConnectionFactory: LettuceConnectionFactory) =
        RedisTemplate<String, Any>().apply {
            keySerializer = StringRedisSerializer()
            valueSerializer = StringRedisSerializer()
            connectionFactory = redisConnectionFactory
        }
}