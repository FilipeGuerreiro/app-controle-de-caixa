package filipe.guerreiro.data.local

import filipe.guerreiro.data.local.dao.AuditLogDao
import filipe.guerreiro.domain.model.AuditLog
import filipe.guerreiro.domain.repository.AuditLogRepository
import kotlinx.coroutines.flow.Flow

class AuditLogRepositoryImpl(
    private val dao: AuditLogDao
) : AuditLogRepository {

    override suspend fun insert(auditLog: AuditLog) {
        dao.insert(auditLog)
    }

    override fun getAuditLogsBySession(sessionId: Long): Flow<List<AuditLog>> {
        return dao.getAuditLogsBySession(sessionId)
    }

    override suspend fun getAuditLogsByTransaction(transactionId: Long): List<AuditLog> {
        return dao.getAuditLogsByTransaction(transactionId)
    }
}
