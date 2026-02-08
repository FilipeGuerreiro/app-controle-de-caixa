package filipe.guerreiro.data.local

import filipe.guerreiro.data.local.dao.CashDao
import filipe.guerreiro.data.local.dao.TransactionDao
import filipe.guerreiro.domain.model.CashSession
import filipe.guerreiro.domain.model.CashStatusType
import filipe.guerreiro.domain.model.SessionBalance
import filipe.guerreiro.domain.repository.CashRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlin.time.Clock

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

    override suspend fun getSuggestedInitialAmount(): Long {
        val lastSession = cashDao.getLastClosedSession() ?: return 0L

        val incomes = transactionDao.getIncomeSum(lastSession.id).first() ?: 0L
        val expenses = transactionDao.getExpenseSum(lastSession.id).first() ?: 0L

        return lastSession.initialAmount + incomes - expenses
    }

    override suspend fun createSession(initialAmount: Long) {
        val activeSession = cashDao.getActiveSession()

        if (activeSession != null) {
            throw IllegalStateException("Já existe um caixa aberto, Feche-o antes de abrir um novo.")
        }

        // TODO: obter `userId` real (session/auth). Atualmente mockamos como 1L.
        val mockUserId = 1L

        val newSession = CashSession(
            userId = mockUserId,
            openingTimeStamp = Clock.System.now(),
            initialAmount = initialAmount,
            status = CashStatusType.OPEN
        )

        cashDao.insertSession(newSession)
    }
}
