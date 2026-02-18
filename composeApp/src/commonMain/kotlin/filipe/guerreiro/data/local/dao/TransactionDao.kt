package filipe.guerreiro.data.local.dao

import androidx.room.*
import filipe.guerreiro.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    // Busca todas as transações de um caixa específico
    @Query("SELECT * FROM transactions WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getTransactionsBySession(sessionId: Long): Flow<List<Transaction>>

    // Busca as N transações mais recentes de um caixa
    @Query("SELECT * FROM transactions WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentTransactions(sessionId: Long, limit: Int): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getAllTransactions(sessionId: Long): Flow<List<Transaction>>

    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT SUM(amount) FROM transactions WHERE sessionId = :sessionId AND type = 'INCOME'")
    fun getIncomeSum(sessionId: Long): Flow<Long?>

    @Query("SELECT SUM(amount) FROM transactions WHERE sessionId = :sessionId AND type = 'EXPENSE'")
    fun getExpenseSum(sessionId: Long): Flow<Long?>

    @Query("""
        SELECT t.* FROM transactions t
        INNER JOIN cash_sessions s ON t.sessionId = s.id
        WHERE s.userId = :userId
        GROUP BY t.categoryId, t.paymentMethodId, t.type
        ORDER BY COUNT(*) DESC
        LIMIT 4
    """)
    fun getTopFrequentTransactions(userId: Long): Flow<List<Transaction>>
}