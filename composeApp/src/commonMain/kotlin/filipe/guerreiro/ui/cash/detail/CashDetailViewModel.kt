package filipe.guerreiro.ui.cash.detail

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.domain.model.toSummaryUi
import filipe.guerreiro.domain.model.toUiModel
import filipe.guerreiro.domain.model.toCurrencyString
import kotlinx.datetime.toLocalDateTime
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import kotlinx.datetime.number
import filipe.guerreiro.domain.repository.CashRepository
import filipe.guerreiro.domain.repository.TransactionRepository
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
    val status: String = "Aberto",
    val openingDate: String = "",
    val closingDate: String? = null,
    val balanceDelta: String = "R$ 0,00",
    val isDeltaPositive: Boolean = true,
    val initialAmountValue: Long = 0L,
    val currentBalanceValue: Long = 0L,
    val totalInflowValue: Long = 0L,
    val totalOutflowValue: Long = 0L
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
    private val cashRepository: CashRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: filipe.guerreiro.domain.repository.CategoryRepository,
    private val paymentMethodRepository: filipe.guerreiro.domain.repository.PaymentMethodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CashDetailUiState(isLoading = true))
    val uiState: StateFlow<CashDetailUiState> = _uiState.asStateFlow()

    init {
        loadSessionData()
    }

    private fun loadSessionData() {
        viewModelScope.launch {
            cashRepository.getSessionById(cashId).collect { session ->
                if (session == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                } else {
                    combine(
                        transactionRepository.getAllTransactions(cashId),
                        cashRepository.getSessionBalance(cashId),
                        categoryRepository.getCategories(session.userId),
                        paymentMethodRepository.getPaymentMethods(session.userId)
                    ) { transactions, balance, categories, paymentMethods ->
                        val items = transactions.map { tx ->
                            val category = categories.find { it.id == tx.categoryId }
                            val paymentMethod = paymentMethods.find { it.id == tx.paymentMethodId }
                            val title = listOfNotNull(category?.name, paymentMethod?.name).joinToString(" • ")

                            val isIncome = tx.type.name == filipe.guerreiro.domain.model.TransactionType.INCOME.name
                            val local = tx.timestamp.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                            val formattedTime = "${local.day}/${local.month.number}/${local.year} ${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"

                            HistoryItemUi(
                                id = tx.id.toString(),
                                title = if (title.isNotEmpty()) title else tx.description,
                                time = formattedTime,
                                method = if (isIncome) "Entrada" else "Saída",
                                amountLabel = tx.amount.toCurrencyString(),
                                isIncome = isIncome,
                                icon = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
                            )
                        }

                        CashDetailUiState(
                            session = session.toUiModel(),
                            summary = balance.toSummaryUi(session.status, session.openingTimeStamp, session.closingTimeStamp),
                            historyItems = items,
                            isLoading = false
                        )
                    }.collect { newState ->
                        _uiState.value = newState
                    }
                }
            }
        }
    }
}
