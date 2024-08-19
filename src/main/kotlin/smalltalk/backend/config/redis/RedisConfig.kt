package smalltalk.backend.config.redis

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RedisConfig {
    @Value("\${spring.redis.host}")
    private lateinit var host: String

    @Value("\${spring.redis.port}")
    private lateinit var port: String

    @Bean
    fun redissonClient(): RedissonClient =
        Redisson.create(
            Config().apply {
                useSingleServer().setAddress("redis://$host:$port")
            }
        )
}