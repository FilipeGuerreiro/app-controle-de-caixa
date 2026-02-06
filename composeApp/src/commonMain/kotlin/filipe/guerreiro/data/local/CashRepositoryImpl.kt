package filipe.guerreiro.data.local

import filipe.guerreiro.data.local.dao.CashDao
import filipe.guerreiro.data.local.dao.TransactionDao
import filipe.guerreiro.domain.model.SessionBalance
import filipe.guerreiro.domain.repository.CashRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class CashRepositoryImpl(
    private val cashDao: CashDao,
    private val transactionDao: TransactionDao
) : CashRepository {

    override fun getSessionBalance(sessionId: Long): Flow<SessionBalance> {
        val sessionFlow = cashDao.getCashSessionById(sessionId)
        val incomesFlow = transactionDao.getIncomeSum(sessionId)
        val expensesFlow = transactionDao.getExpenseSum(sessionId)

        return combine(sessionFlow, incomesFlow, expensesFlow) { session, incomes, expenses ->
            val initial = session?.initialAmount ?: 0L
            val totalIn = incomes ?: 0L
            val totalOut = expenses ?: 0L

            SessionBalance(
                initial = initial,
                totalIncomes = totalIn,
                totalExpenses = totalOut,
                currentBalance = initial + totalIn - totalOut
            )
        }
    }
}
