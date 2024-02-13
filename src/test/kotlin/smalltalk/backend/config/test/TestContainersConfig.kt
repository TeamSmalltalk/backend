package smalltalk.backend.config.test

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Configuration
@Testcontainers
@ActiveProfiles("test")
class TestContainersConfig {
    companion object {

        private val logger = KotlinLogging.logger {  }

        private const val LOCAL_REDIS_PORT = 6379
        private const val REDIS_IMAGE = "redis:latest"
        private const val LOCAL_REDIS_HOST_PATH = "spring.data.redis.host"
        private const val LOCAL_REDIS_PORT_PATH = "spring.data.redis.port"
        private const val REDIS_CONTAINER_PORT = 6379

        @Container
        val redisContainer =
            GenericContainer<Nothing>(REDIS_IMAGE).apply {
                withExposedPorts(LOCAL_REDIS_PORT)
            }

        @JvmStatic
        @DynamicPropertySource
        fun registerRedisProperties(registry: DynamicPropertyRegistry) {

            registry.add(LOCAL_REDIS_HOST_PATH) { redisContainer.host }
            registry.add(LOCAL_REDIS_PORT_PATH) { redisContainer.getMappedPort(REDIS_CONTAINER_PORT).toString() }
        }

        init {
            redisContainer.start()
            logger.info { "Redis container info = ${redisContainer.containerInfo}" }
        }
    }
}