package filipe.guerreiro.domain.repository

import filipe.guerreiro.domain.model.Transaction
import filipe.guerreiro.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getRecentTransactions(sessionId: Long, limit: Int = 5): Flow<List<Transaction>>
    fun getAllTransactions(sessionId: Long): Flow<List<Transaction>>
    fun getTopFrequentTransactions(userId: Long): Flow<List<Transaction>>

    suspend fun addTransactionForUserCurrentSession(
        userId: Long,
        categoryId: Long,
        paymentMethodId: Long,
        amount: Long,
        description: String,
        type: TransactionType
    )
}