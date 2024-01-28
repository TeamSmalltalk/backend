package smalltalk.backend.config.redis

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import java.sql.SQLException
import kotlin.jvm.Throws

@Configuration
@EnableTransactionManagement  // 다른 DBMS 없이 redis 단독으로 사용
class RedisConfig(
    @Value("\${spring.data.redis.host}")
    private val host: String,
    @Value("\${spring.data.redis.port}")
    private val port: Int
) {

    @Bean
    fun redisConnectionFactory() = LettuceConnectionFactory(host, port)

    @Bean
    @Throws(SQLException::class)
    fun platformTransactionManager() = DataSourceTransactionManagerAutoConfiguration()

    @Bean
    fun redisTemplate(): RedisTemplate<String, String> {

        val redisTemplate: RedisTemplate<String, String> = RedisTemplate()
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = StringRedisSerializer()
        redisTemplate.connectionFactory = redisConnectionFactory()
        redisTemplate.setEnableTransactionSupport(true)  // redis transaction

        return redisTemplate
    }
}