package smalltalk.backend.config.redis

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import smalltalk.backend.domain.room.Room

@Configuration
class RedisConfig {

    // Spring 에서 RedisConnectionFactory Bean 자동 구성
    @Bean
    fun redisTemplate(redisConnectionFactory: LettuceConnectionFactory) =
        RedisTemplate<String, Room>().apply {
            keySerializer = StringRedisSerializer()
            valueSerializer = Jackson2JsonRedisSerializer(Room::class.java)
            connectionFactory = redisConnectionFactory
        }
}