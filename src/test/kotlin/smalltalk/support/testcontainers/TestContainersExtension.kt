package smalltalk.support.testcontainers

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.GenericContainer

abstract class TestContainersExtension {
    companion object {
        private const val HOST_PROPERTY_KEY = "spring.data.redis.host"
        private const val PORT_PROPERTY_KEY = "spring.data.redis.port"
    }

    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            GenericContainer<Nothing>("redis:latest").apply {
                withExposedPorts(6379)
            }.run {
                start()
                TestPropertyValues.of(
                    "$HOST_PROPERTY_KEY=${host}",
                    "$PORT_PROPERTY_KEY=${firstMappedPort}"
                ).applyTo(applicationContext.environment)
            }
        }
    }
}