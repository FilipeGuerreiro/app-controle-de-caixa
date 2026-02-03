package filipe.guerreiro.domain.model

data class CashBoxSummary(
    val session: CashSession,
    val transactions: List<Transaction>
) {
    val currentBalance: Double
        get() = session.initialAmount + transactions.sumOf {
            if (it.type == TransactionType.INCOME) it.amount else -it.amount
        }
}