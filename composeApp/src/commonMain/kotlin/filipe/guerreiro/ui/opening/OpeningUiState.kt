package filipe.guerreiro.ui.opening

data class OpeningUiState(
    val initialAmountInput: String = "",
    val suggestedAmount: Long = 0L,
    val isLoading: Boolean = false,
    val isSessionOpened: Boolean = false,
    val errorMessage: String? = null
)