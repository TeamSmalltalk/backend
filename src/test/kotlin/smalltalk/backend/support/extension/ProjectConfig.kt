package smalltalk.backend.support.extension

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.spring.SpringAutowireConstructorExtension

class ProjectConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(SpringAutowireConstructorExtension)
}