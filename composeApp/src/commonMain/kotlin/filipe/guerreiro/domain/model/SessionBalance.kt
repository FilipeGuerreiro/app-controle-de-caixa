package filipe.guerreiro.domain.model

data class SessionBalance(
    val initial: Long,
    val totalIncomes: Long,
    val totalExpenses: Long,
    val currentBalance: Long
)
