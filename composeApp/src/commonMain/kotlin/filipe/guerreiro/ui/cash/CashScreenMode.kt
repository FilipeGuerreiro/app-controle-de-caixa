package filipe.guerreiro.ui.cash

sealed interface CashScreenMode {
    data object List : CashScreenMode
    data class Detail(val cashId: Long) : CashScreenMode
}