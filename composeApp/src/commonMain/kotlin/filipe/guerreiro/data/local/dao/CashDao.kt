package filipe.guerreiro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import filipe.guerreiro.domain.model.CashSession
import kotlinx.coroutines.flow.Flow

@Dao
interface CashDao {

    // Busca a sessão que estiver aberta (status = 'OPEN')
    // Usamos Flow para que a UI se atualize sozinha se o status mudar
    @Query("SELECT * FROM cash_sessions WHERE status = 'OPEN' LIMIT 1")
    fun getActiveSession(): Flow<CashSession?>

    @Query("SELECT * FROM cash_sessions WHERE id = :sessionId LIMIT 1")
    fun getCashSessionById(sessionId: Long): Flow<CashSession?>

    @Insert
    suspend fun insertSession(session: CashSession): Long

    @Update
    suspend fun updateSession(session: CashSession)

    @Query("SELECT * FROM cash_sessions ORDER BY openingTimeStamp DESC")
    fun getAllSessions(): Flow<List<CashSession>>
}