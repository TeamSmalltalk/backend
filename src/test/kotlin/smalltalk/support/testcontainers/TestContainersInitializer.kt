package smalltalk.support.testcontainers

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.GenericContainer

class TestContainersInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    companion object {
        private const val REDIS_IMAGE = "redis:latest"
        private const val REDIS_EXPOSE_PORT = 6379
        private const val HOST_PROPERTY_KEY = "spring.data.redis.host"
        private const val PORT_PROPERTY_KEY = "spring.data.redis.port"
    }

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        GenericContainer<Nothing>(REDIS_IMAGE).apply { withExposedPorts(REDIS_EXPOSE_PORT) }.run {
            start()
            TestPropertyValues.of(
                "$HOST_PROPERTY_KEY=${host}",
                "$PORT_PROPERTY_KEY=${firstMappedPort}"
            ).applyTo(applicationContext.environment)
        }
    }
}