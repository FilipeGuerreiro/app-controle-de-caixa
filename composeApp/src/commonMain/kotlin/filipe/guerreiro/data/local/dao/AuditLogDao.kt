package filipe.guerreiro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import filipe.guerreiro.domain.model.AuditLog
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {
    @Insert
    suspend fun insert(auditLog: AuditLog)

    @Query("SELECT * FROM audit_logs WHERE sessionId = :sessionId ORDER BY timestamp DESC")
    fun getAuditLogsBySession(sessionId: Long): Flow<List<AuditLog>>

    @Query("SELECT * FROM audit_logs WHERE transactionId = :transactionId ORDER BY timestamp DESC")
    suspend fun getAuditLogsByTransaction(transactionId: Long): List<AuditLog>
}
