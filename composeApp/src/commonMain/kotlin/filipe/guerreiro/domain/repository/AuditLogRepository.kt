package filipe.guerreiro.domain.repository

import filipe.guerreiro.domain.model.AuditLog
import kotlinx.coroutines.flow.Flow

interface AuditLogRepository {
    suspend fun insert(auditLog: AuditLog)
    fun getAuditLogsBySession(sessionId: Long): Flow<List<AuditLog>>
    suspend fun getAuditLogsByTransaction(transactionId: Long): List<AuditLog>
}
