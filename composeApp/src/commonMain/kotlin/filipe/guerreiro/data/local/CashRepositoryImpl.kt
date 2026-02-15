package filipe.guerreiro.data.local

import filipe.guerreiro.data.local.dao.CashDao
import filipe.guerreiro.data.local.dao.TransactionDao
import filipe.guerreiro.domain.model.CashSession
import filipe.guerreiro.domain.model.CashStatusType
import filipe.guerreiro.domain.model.SessionBalance
import filipe.guerreiro.domain.model.Transaction
import filipe.guerreiro.domain.repository.CashRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.Instant

class CashRepositoryImpl(
    private val cashDao: CashDao,
    private val transactionDao: TransactionDao
) : CashRepository {

    override fun getActiveSession(userId: Long): Flow<CashSession?> {
        return cashDao.getActiveSession(userId)
    }

    override fun getAllSessions(userId: Long): Flow<List<CashSession>> {
        return cashDao.getAllSessions(userId)
    }

    override fun getSessionById(sessionId: Long): Flow<CashSession?> {
        return cashDao.getCashSessionById(sessionId)
    }

    override fun getCurrentCashSession(userId: Long): Flow<CashSession?> {
        return cashDao.getCurrentCashSession(userId)
    }

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

    override fun getRecentTransactions(sessionId: Long, limit: Int): Flow<List<Transaction>> {
        return transactionDao.getRecentTransactions(sessionId, limit)
    }

    override fun hasAnyCashHistory(userId: Long): Flow<Boolean> {
        return cashDao.getSessionCount(userId).map { count -> count > 0 }
    }

    override suspend fun getSuggestedInitialAmount(userId: Long): Long {
        val lastSession = cashDao.getLastClosedSession(userId) ?: return 0L

        val incomes = transactionDao.getIncomeSum(lastSession.id).first() ?: 0L
        val expenses = transactionDao.getExpenseSum(lastSession.id).first() ?: 0L

        return lastSession.initialAmount + incomes - expenses
    }

    override suspend fun createSession(initialAmount: Long, userId: Long) {
        val activeSession = cashDao.getCurrentCashSession(userId).first()

        if (activeSession != null && activeSession.status == CashStatusType.OPEN) {
            throw IllegalStateException("Já existe um caixa aberto, Feche-o antes de abrir um novo.")
        }

        val newSession = CashSession(
            userId = userId,
            openingTimeStamp = Clock.System.now(),
            initialAmount = initialAmount,
            status = CashStatusType.OPEN
        )

        cashDao.insertSession(newSession)
    }

    override suspend fun closeSession(userId: Long) {
        val activeSession = cashDao.getCurrentCashSession(userId).first() ?: throw IllegalStateException("Nenhum caixa aberto encontrado.")

        if (activeSession.status == CashStatusType.CLOSED) {
            throw IllegalStateException("O caixa atual já está fechado.")
        }

        val closingTime = Clock.System.now()
        cashDao.closeSession(activeSession.id, closingTime = closingTime)

    }
}
