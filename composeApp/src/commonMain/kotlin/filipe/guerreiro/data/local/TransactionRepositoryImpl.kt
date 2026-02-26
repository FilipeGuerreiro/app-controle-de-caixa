package filipe.guerreiro.data.local

import filipe.guerreiro.data.local.dao.CashDao
import filipe.guerreiro.data.local.dao.TransactionDao
import filipe.guerreiro.domain.model.Transaction
import filipe.guerreiro.domain.model.TransactionType
import filipe.guerreiro.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class TransactionRepositoryImpl(
    private val cashDao: CashDao,
    private val transactionDao: TransactionDao
) : TransactionRepository {

    override fun getRecentTransactions(sessionId: Long, limit: Int): Flow<List<Transaction>> {
        return transactionDao.getRecentTransactions(sessionId, limit)
    }

    override fun getAllTransactions(sessionId: Long): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions(sessionId)
    }

    override fun getTopFrequentTransactions(userId: Long): Flow<List<Transaction>> {
        return transactionDao.getTopFrequentTransactions(userId)
    }

    override suspend fun addTransactionForUserCurrentSession(
        userId: Long,
        categoryId: Long,
        paymentMethodId: Long,
        amount: Long,
        description: String,
        type: TransactionType
    ) {
        withContext(Dispatchers.IO) {
            val session = cashDao.getCurrentCashSession(userId).first()
                ?: throw IllegalStateException("Nenhum caixa aberto encontrado para registrar o lançamento.")

            val transaction = Transaction(
                sessionId = session.id,
                categoryId = categoryId,
                paymentMethodId = paymentMethodId,
                amount = amount,
                description = description,
                type = type,
                timestamp = Clock.System.now()
            )

            transactionDao.insertTransaction(transaction)
        }
    }

    override suspend fun updateTransaction(transaction: Transaction) {
        withContext(Dispatchers.IO) {
            transactionDao.updateTransaction(transaction)
        }
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        withContext(Dispatchers.IO) {
            transactionDao.softDeleteTransaction(transaction.id)
        }
    }
}
