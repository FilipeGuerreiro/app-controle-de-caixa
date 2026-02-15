package filipe.guerreiro.domain.repository

import filipe.guerreiro.domain.model.CashSession
import filipe.guerreiro.domain.model.SessionBalance
import filipe.guerreiro.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface CashRepository {
    fun getActiveSession(userId: Long): Flow<CashSession?>
    fun getCurrentCashSession(userId: Long): Flow<CashSession?>
    fun getSessionBalance(sessionId: Long): Flow<SessionBalance>
    fun getRecentTransactions(sessionId: Long, limit: Int = 5): Flow<List<Transaction>>
    fun hasAnyCashHistory(userId: Long): Flow<Boolean>
    fun getAllSessions(userId: Long): Flow<List<CashSession>>
    fun getSessionById(sessionId: Long): Flow<CashSession?>
    suspend fun getSuggestedInitialAmount(userId: Long): Long
    suspend fun createSession(initialAmount: Long, userId: Long)
    suspend fun closeSession(userId: Long)

}