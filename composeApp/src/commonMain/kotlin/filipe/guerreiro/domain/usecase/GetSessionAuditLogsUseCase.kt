package filipe.guerreiro.domain.usecase

import filipe.guerreiro.domain.model.AuditLog
import filipe.guerreiro.domain.repository.AuditLogRepository
import kotlinx.coroutines.flow.Flow

class GetSessionAuditLogsUseCase(
    private val auditLogRepository: AuditLogRepository
) {
    operator fun invoke(sessionId: Long): Flow<List<AuditLog>> {
        return auditLogRepository.getAuditLogsBySession(sessionId)
    }
}
