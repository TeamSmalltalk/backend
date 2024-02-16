package smalltalk.backend.config.redis

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.test.context.TestConfiguration
import org.testcontainers.containers.GenericContainer

@TestConfiguration
class RedisContainerConfig {

    companion object {

        private val logger = KotlinLogging.logger { }

        private const val REDIS_IMAGE = "redis:latest"
        private const val REDIS_CONTAINER_PORT = 6379
        private const val LOCAL_REDIS_HOST_PATH = "spring.redis.host"
        private const val LOCAL_REDIS_PORT_PATH = "spring.redis.port"


        init {
            GenericContainer<Nothing>(REDIS_IMAGE).apply {
                withExposedPorts(REDIS_CONTAINER_PORT)
                this.start()
                logger.info { "Redis container start" }
                System.setProperty(LOCAL_REDIS_HOST_PATH, this.host)
                System.setProperty(LOCAL_REDIS_PORT_PATH, this.getMappedPort(REDIS_CONTAINER_PORT).toString())
                logger.info { "Redis container info = ${this.containerInfo}" }
            }
        }
    }
}