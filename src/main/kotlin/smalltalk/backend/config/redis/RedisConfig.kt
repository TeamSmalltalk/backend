package smalltalk.backend.config.redis

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate

@Configuration
class RedisConfig {
    @Value("\${spring.data.redis.host}")
    private lateinit var host: String
    @Value("\${spring.data.redis.port}")
    private lateinit var port: String

//    @Bean
//    fun redissonClient(): RedissonClient =
//        Redisson.create(
//            Config().apply {
//                useSingleServer().setAddress("redis://$host:$port")
//            }
//        )

    @Bean
    fun objectMapper() = jacksonObjectMapper()

    @Bean
    fun redisConnectionFactory() = LettuceConnectionFactory(host, port.toInt())

    @Bean
    fun redisTemplate() =
        StringRedisTemplate().apply { connectionFactory = redisConnectionFactory() }
}