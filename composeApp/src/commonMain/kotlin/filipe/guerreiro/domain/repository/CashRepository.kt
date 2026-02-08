package filipe.guerreiro.domain.repository

import filipe.guerreiro.domain.model.SessionBalance
import kotlinx.coroutines.flow.Flow

interface CashRepository {
    fun getSessionBalance(sessionId: Long): Flow<SessionBalance>
    suspend fun getSuggestedInitialAmount(): Long
    suspend fun createSession(initialAmount: Long)
}