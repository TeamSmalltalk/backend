package smalltalk.support

import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import smalltalk.support.testcontainers.TestContainersExtension

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
@ContextConfiguration(initializers = [TestContainersExtension.Initializer::class])
@ActiveProfiles("test")
@DirtiesContext
annotation class EnableTestContainers()