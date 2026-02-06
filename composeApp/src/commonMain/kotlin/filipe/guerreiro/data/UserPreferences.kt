package filipe.guerreiro.data

interface UserPreferences {
    suspend fun isUserRegistered(): Boolean
    suspend fun setUserRegistered(value: Boolean)
}