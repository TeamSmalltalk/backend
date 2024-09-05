package smalltalk.backend.support.redis

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.test.context.TestConfiguration
import org.testcontainers.containers.GenericContainer

@TestConfiguration
class RedisContainerConfig {
    companion object {
        private val logger = KotlinLogging.logger { }
        private const val REDIS_IMAGE = "redis:latest"
        private const val REDIS_CONTAINER_PORT = 6379
        private const val LOCAL_REDIS_HOST_PATH = "spring.data.redis.host"
        private const val LOCAL_REDIS_PORT_PATH = "spring.data.redis.port"
        init {
            GenericContainer<Nothing>(REDIS_IMAGE).apply {
                withExposedPorts(REDIS_CONTAINER_PORT)
                start()
                logger.info { "Redis container start" }
                System.setProperty(LOCAL_REDIS_HOST_PATH, host)
                System.setProperty(LOCAL_REDIS_PORT_PATH, getMappedPort(REDIS_CONTAINER_PORT).toString())
                logger.info { "Redis container info = $containerInfo" }
            }
        }
    }
}