package filipe.guerreiro.domain.usecase

import filipe.guerreiro.domain.model.AuditActionType
import filipe.guerreiro.domain.model.AuditLog
import filipe.guerreiro.domain.model.Transaction
import filipe.guerreiro.domain.repository.AuditLogRepository
import filipe.guerreiro.domain.repository.TransactionRepository
import filipe.guerreiro.domain.model.toCurrencyString
import kotlin.time.Clock

class DeleteTransactionUseCase(
    private val transactionRepository: TransactionRepository,
    private val auditLogRepository: AuditLogRepository
) {
    suspend operator fun invoke(transaction: Transaction, wasClosed: Boolean, reason: String?) {
        if (wasClosed) {
            val desc = transaction.description.ifBlank { "Sem descrição" }
            val auditLog = AuditLog(
                sessionId = transaction.sessionId,
                transactionId = transaction.id,
                actionType = AuditActionType.DELETE,
                oldValue = "Valor: ${transaction.amount.toCurrencyString()} | Descrição: $desc",
                newValue = "Excluído",
                reason = reason ?: "Exclusão retroativa manual",
                timestamp = Clock.System.now()
            )
            auditLogRepository.insert(auditLog)
        }
        transactionRepository.deleteTransaction(transaction)
    }
}
