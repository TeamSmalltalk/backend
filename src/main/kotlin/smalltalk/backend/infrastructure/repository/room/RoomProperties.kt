package smalltalk.backend.infrastructure.repository.room

interface RoomProperties {
    fun getKeyPrefix(): String
    fun getCounterKey(): String
    fun getProviderKeyPostfix(): String
    fun getLibraryNameOfAddMember(): String
    fun getLibraryNameOfDeleteMember(): String
    fun getLibraryFunctionKeyOfAddMember(): String
    fun getLibraryFunctionKeyOfDeleteMember(): String
    fun getInitNumberOfMember(): Int
    fun getLimitNumberOfMember(): Int
}