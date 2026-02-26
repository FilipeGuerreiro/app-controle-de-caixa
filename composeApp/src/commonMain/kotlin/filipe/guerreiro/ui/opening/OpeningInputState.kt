package filipe.guerreiro.ui.opening

data class OpeningInputState(
    val amountInCents: Long = 0L,
    val isAmountNegative: Boolean = false,
    val dailyGoalInCents: Long = 0L,
    val errorMessage: String? = null
)
