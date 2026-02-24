package filipe.guerreiro.data

interface SessionPreferences {
    suspend fun getLoggedUserId(): Long?
    suspend fun setLoggedUserId(userId: Long)
    suspend fun getDefaultDailyGoal(): Long?
    suspend fun setDefaultDailyGoal(amount: Long)
    suspend fun clearSession()
}