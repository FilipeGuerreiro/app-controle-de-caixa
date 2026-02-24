package filipe.guerreiro.ui.opening

data class OpeningUiState(
    val suggestedAmount: Long = 0L,
    val displayAmount: String = "0,00",
    val amountInCents: Long = 0L,
    val displayDailyGoal: String = "0,00",
    val dailyGoalInCents: Long = 0L,
    val isLoading: Boolean = false,
    val isSessionOpened: Boolean = false,
    val openedSessionId: Long? = null,
    val isLogged: Boolean = false,
    val errorMessage: String? = null
)