package smalltalk.backend.config.property

interface RoomProperties {
    fun getKeyPrefix(): String
    fun getKeyOfCounter(): String
    fun getKeyPostfixOfProvider(): String
    fun getLibraryNameOfAddMember(): String
    fun getLibraryNameOfDeleteMember(): String
    fun getLibraryFunctionKeyOfAddMember(): String
    fun getLibraryFunctionKeyOfDeleteMember(): String
    fun getInitNumberOfMember(): Int
    fun getLimitNumberOfMember(): Int
}