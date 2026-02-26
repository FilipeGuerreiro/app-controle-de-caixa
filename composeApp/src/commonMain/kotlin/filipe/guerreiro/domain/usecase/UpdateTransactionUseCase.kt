package filipe.guerreiro.domain.usecase

import filipe.guerreiro.domain.model.AuditActionType
import filipe.guerreiro.domain.model.AuditLog
import filipe.guerreiro.domain.model.Transaction
import filipe.guerreiro.domain.repository.AuditLogRepository
import filipe.guerreiro.domain.repository.TransactionRepository
import filipe.guerreiro.domain.model.toCurrencyString
import kotlin.time.Clock

class UpdateTransactionUseCase(
    private val transactionRepository: TransactionRepository,
    private val auditLogRepository: AuditLogRepository
) {
    suspend operator fun invoke(
        oldTransaction: Transaction,
        newTransaction: Transaction,
        wasClosed: Boolean,
        reason: String?
    ) {
        if (wasClosed) {
            val oldDesc = oldTransaction.description.ifBlank { "Sem descrição" }
            val newDesc = newTransaction.description.ifBlank { "Sem descrição" }
            
            val auditLog = AuditLog(
                sessionId = newTransaction.sessionId,
                transactionId = newTransaction.id,
                actionType = AuditActionType.UPDATE,
                oldValue = "Valor: ${oldTransaction.amount.toCurrencyString()} | Descrição: $oldDesc",
                newValue = "Valor: ${newTransaction.amount.toCurrencyString()} | Descrição: $newDesc",
                reason = reason ?: "Edição retroativa manual",
                timestamp = Clock.System.now()
            )
            auditLogRepository.insert(auditLog)
        }
        transactionRepository.updateTransaction(newTransaction)
    }
}
