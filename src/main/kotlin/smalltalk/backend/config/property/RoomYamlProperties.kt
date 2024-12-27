package smalltalk.backend.config.property

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "room")
data class RoomYamlProperties(
    val key: KeyYamlProperties,
    val library: LibraryYamlProperties,
    val member: MemberYamlProperties
) : RoomProperties {
    data class KeyYamlProperties(
        val prefix: String,
        val counter: String,
        val postfixOfProvider: String
    )

    data class LibraryYamlProperties(
        val nameOfAddMember: String,
        val nameOfDeleteMember: String,
        val functionKeyOfAddMember: String,
        val functionKeyOfDeleteMember: String
    )

    data class MemberYamlProperties(
        val init: Int,
        val limit: Int
    )

    override fun getKeyPrefix() = key.prefix
    override fun getKeyOfCounter() = key.counter
    override fun getKeyPostfixOfProvider() = key.postfixOfProvider
    override fun getLibraryNameOfAddMember() = library.nameOfAddMember
    override fun getLibraryNameOfDeleteMember() = library.nameOfDeleteMember
    override fun getLibraryFunctionKeyOfAddMember() = library.functionKeyOfAddMember
    override fun getLibraryFunctionKeyOfDeleteMember() = library.functionKeyOfDeleteMember
    override fun getInitNumberOfMember() = member.init
    override fun getLimitNumberOfMember() = member.limit
}