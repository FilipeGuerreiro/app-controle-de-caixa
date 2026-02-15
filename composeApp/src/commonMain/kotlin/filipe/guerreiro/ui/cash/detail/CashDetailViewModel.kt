package filipe.guerreiro.ui.cash.detail

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.domain.model.toHistoryUi
import filipe.guerreiro.domain.model.toSummaryUi
import filipe.guerreiro.domain.model.toUiModel
import filipe.guerreiro.domain.repository.CashRepository
import filipe.guerreiro.ui.cash.listing.CashSessionUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class CashDetailUiState(
    val session: CashSessionUi? = null,
    val summary: CashSummaryUi = CashSummaryUi(),
    val historyItems: List<HistoryItemUi> = emptyList(),
    val isLoading: Boolean = true
)

data class CashSummaryUi(
    val initialAmount: String = "R$ 0,00",
    val currentBalance: String = "R$ 0,00",
    val totalInflow: String = "R$ 0,00",
    val totalOutflow: String = "R$ 0,00",
    val status: String = "Aberto"
)

data class HistoryItemUi(
    val id: String,
    val title: String,
    val time: String,
    val method: String,
    val amountLabel: String,
    val isIncome: Boolean,
    val icon: ImageVector
)

class CashDetailViewModel(
    private val cashId: Long,
    private val cashRepository: CashRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CashDetailUiState(isLoading = true))
    val uiState: StateFlow<CashDetailUiState> = _uiState.asStateFlow()

    init {
        loadSessionData()
    }

    private fun loadSessionData() {
        viewModelScope.launch {
            combine(
                cashRepository.getSessionById(cashId),
                cashRepository.getAllTransactions(cashId),
                cashRepository.getSessionBalance(cashId)
            ) { session, transactions, balance ->
                if (session != null) {
                    CashDetailUiState(
                        session = session.toUiModel(),
                        summary = balance.toSummaryUi(session.status),
                        historyItems = transactions.map { it.toHistoryUi() },
                        isLoading = false
                    )
                } else {
                    _uiState.value.copy(isLoading = false)
                }
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
}