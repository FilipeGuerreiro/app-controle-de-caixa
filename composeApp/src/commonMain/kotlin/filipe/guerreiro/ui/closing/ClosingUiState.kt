package filipe.guerreiro.ui.closing

data class ClosingUiState (
    val isLoading: Boolean = false,
    val isSessionClosed: Boolean = false,
    val isLogged: Boolean = false,
    val errorMessage: String? = null,
    val closingData: ClosingDataState = ClosingDataState()
)