package filipe.guerreiro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import filipe.guerreiro.domain.model.CashSession
import filipe.guerreiro.domain.model.CashStatusType
import kotlinx.coroutines.flow.Flow
import kotlin.time.Instant

@Dao
interface CashDao {

    // Busca a sessão que estiver aberta (status = 'OPEN')
    // Usamos Flow para que a UI se atualize sozinha se o status mudar
    @Query("SELECT * FROM cash_sessions WHERE status = 'OPEN' AND userId = :userId LIMIT 1")
    fun getActiveSession(userId: Long): Flow<CashSession?>

    @Query("SELECT * FROM cash_sessions WHERE userId = :userId ORDER BY openingTimeStamp DESC")
    fun getCurrentCashSession(userId: Long): Flow<CashSession?>

    @Query("SELECT * FROM cash_sessions WHERE id = :sessionId LIMIT 1")
    fun getCashSessionById(sessionId: Long): Flow<CashSession?>

    @Insert
    suspend fun insertSession(session: CashSession): Long

    @Update
    suspend fun updateSession(session: CashSession)

    @Query("SELECT * FROM cash_sessions WHERE userId = :userId ORDER BY openingTimeStamp DESC")
    fun getAllSessions(userId: Long): Flow<List<CashSession>>

    @Query("SELECT * FROM cash_sessions WHERE status = 'CLOSED' AND userId = :userId ORDER BY closingTimeStamp DESC LIMIT 1")
    suspend fun getLastClosedSession(userId: Long): CashSession?

    // Verifica se existe algum caixa no histórico (para primeiro acesso)
    @Query("SELECT COUNT(*) FROM cash_sessions WHERE userId = :userId")
    fun getSessionCount(userId: Long): Flow<Int>

    @Query("UPDATE cash_sessions SET status = :status, closingTimeStamp = :closingTime WHERE id = :sessionId")
    suspend fun closeSession(sessionId: Long, status: CashStatusType, closingTime: Instant)

    @Query("UPDATE cash_sessions SET dailyGoalAmount = :amount WHERE id = :sessionId")
    suspend fun updateDailyGoal(sessionId: Long, amount: Long?)

    @Query("SELECT * FROM cash_sessions WHERE userId = :userId AND openingTimeStamp >= :startMs AND openingTimeStamp < :endMs ORDER BY openingTimeStamp ASC")
    fun getSessionsByDateRange(userId: Long, startMs: Long, endMs: Long): Flow<List<CashSession>>

    @Query("SELECT * FROM cash_sessions WHERE userId = :userId ORDER BY openingTimeStamp ASC")
    suspend fun getAllSessionsSuspend(userId: Long): List<CashSession>
}