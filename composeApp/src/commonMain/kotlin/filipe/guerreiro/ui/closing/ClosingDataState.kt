package filipe.guerreiro.ui.closing

data class ClosingDataState(
    val initialBalance: Long? = null,
    val finalBalance: Long? = null,
    val incomeBalance: Long? = null,
    val expenseBalance: Long? = null,
    val transactionCount: Int = 0,
    val openingTimestamp: Long? = null,
    val errorMessage: String? = null
)
