package filipe.guerreiro.data.local.dao

import androidx.room.*
import filipe.guerreiro.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    // Busca todas as transações de um caixa específico
    @Query("SELECT * FROM transactions WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getTransactionsBySession(sessionId: Long): Flow<List<Transaction>>

    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
}