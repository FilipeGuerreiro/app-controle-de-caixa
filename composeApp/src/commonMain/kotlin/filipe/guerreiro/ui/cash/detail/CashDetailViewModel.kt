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
import androidx.compose.material.icons.filled.Flag
import kotlinx.datetime.number
import filipe.guerreiro.domain.repository.CashRepository
import filipe.guerreiro.domain.repository.TransactionRepository
import filipe.guerreiro.ui.cash.listing.CashSessionUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

data class CashDetailUiState(
    val session: CashSessionUi? = null,
    val summary: CashSummaryUi = CashSummaryUi(),
    val historyItems: List<HistoryItemUi> = emptyList(),
    val categoryBalances: List<BreakdownItemUi> = emptyList(),
    val paymentMethodBalances: List<BreakdownItemUi> = emptyList(),
    val goals: List<GoalUi> = emptyList(),
    val transactionDetails: Map<String, TransactionDetailUi> = emptyMap(),
    val selectedTransaction: TransactionDetailUi? = null,
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
    val totalOutflowValue: Long = 0L,
    val dailyGoalAmount: Long? = null,
    val isLatestSession: Boolean = false
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

data class TransactionDetailUi(
    val id: String,
    val title: String,
    val typeName: String,
    val amountFull: String,
    val dateFull: String,
    val categoryName: String?,
    val paymentMethodName: String?,
    val description: String,
    val isIncome: Boolean,
    val icon: ImageVector
)

data class BreakdownItemUi(
    val name: String,
    val amountFormatted: String,
    val amountValue: Long,
    val isIncome: Boolean,
    val progress: Float
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadSessionData() {
        viewModelScope.launch {
            cashRepository.getSessionById(cashId)
                .flatMapLatest { session ->
                    if (session == null) {
                        flowOf(_uiState.value.copy(isLoading = false))
                    } else {
                        combine(
                        transactionRepository.getAllTransactions(cashId),
                        cashRepository.getSessionBalance(cashId),
                        categoryRepository.getCategories(session.userId),
                        paymentMethodRepository.getPaymentMethods(session.userId),
                        cashRepository.getCurrentCashSession(session.userId)
                    ) { transactions, balance, categories, paymentMethods, latestSession ->
                        val mappedTransactions = transactions.map { tx ->
                            val category = categories.find { it.id == tx.categoryId }
                            val paymentMethod = paymentMethods.find { it.id == tx.paymentMethodId }
                            val title = listOfNotNull(category?.name, paymentMethod?.name).joinToString(" • ")

                            val isIncome = tx.type.name == filipe.guerreiro.domain.model.TransactionType.INCOME.name
                            val local = tx.timestamp.toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
                            val formattedTime = "${local.day}/${local.month.number}/${local.year} ${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"
                            val dateFull = "${local.day.toString().padStart(2, '0')}/${local.month.number.toString().padStart(2, '0')}/${local.year} às ${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"

                            val historyItem = HistoryItemUi(
                                id = tx.id.toString(),
                                title = if (title.isNotEmpty()) title else tx.description,
                                time = formattedTime,
                                method = if (isIncome) "Entrada" else "Saída",
                                amountLabel = tx.amount.toCurrencyString(),
                                isIncome = isIncome,
                                icon = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
                            )
                            
                            val detailItem = TransactionDetailUi(
                                id = tx.id.toString(),
                                title = title.ifEmpty { "Sem Categoria" },
                                typeName = if (isIncome) "Entrada de Caixa" else "Saída de Caixa",
                                amountFull = tx.amount.toCurrencyString(),
                                dateFull = dateFull,
                                categoryName = category?.name,
                                paymentMethodName = paymentMethod?.name,
                                description = tx.description.ifEmpty { "Nenhuma descrição fornecida." },
                                isIncome = isIncome,
                                icon = historyItem.icon
                            )
                            
                            Pair(historyItem, detailItem)
                        }

                        val items = mappedTransactions.map { it.first }
                        val detailsMap = mappedTransactions.associate { it.second.id to it.second }

                            // Mocked data for Category Balances
                            val mockCategoryBalances = listOf(
                                BreakdownItemUi(
                                    name = "Refrigerante",
                                    amountFormatted = "R$ 200,00",
                                    amountValue = 20000,
                                    isIncome = true,
                                    progress = 0.66f
                                ),
                                BreakdownItemUi(
                                    name = "Espetinho",
                                    amountFormatted = "R$ 100,00",
                                    amountValue = 10000,
                                    isIncome = true,
                                    progress = 0.33f
                                ),
                                BreakdownItemUi(
                                    name = "Carvão",
                                    amountFormatted = "R$ 50,00",
                                    amountValue = 5000,
                                    isIncome = false,
                                    progress = 0.33f
                                ),
                                BreakdownItemUi(
                                    name = "Bebidas (Fornecedor)",
                                    amountFormatted = "R$ 100,00",
                                    amountValue = 10000,
                                    isIncome = false,
                                    progress = 0.66f
                                )
                            )

                            // Mocked data for Payment Method Balances
                            val mockPaymentMethodBalances = listOf(
                                BreakdownItemUi(
                                    name = "Dinheiro",
                                    amountFormatted = "R$ 150,00",
                                    amountValue = 15000,
                                    isIncome = true,
                                    progress = 0.50f
                                ),
                                BreakdownItemUi(
                                    name = "PIX",
                                    amountFormatted = "R$ 100,00",
                                    amountValue = 10000,
                                    isIncome = true,
                                    progress = 0.33f
                                ),
                                BreakdownItemUi(
                                    name = "Cartão de Crédito",
                                    amountFormatted = "R$ 50,00",
                                    amountValue = 5000,
                                    isIncome = true,
                                    progress = 0.17f
                                ),
                                BreakdownItemUi(
                                    name = "Dinheiro",
                                    amountFormatted = "R$ 100,00",
                                    amountValue = 10000,
                                    isIncome = false,
                                    progress = 0.66f
                                ),
                                BreakdownItemUi(
                                    name = "PIX",
                                    amountFormatted = "R$ 50,00",
                                    amountValue = 5000,
                                    isIncome = false,
                                    progress = 0.33f
                                )
                            )

                            val summaryUi = balance.toSummaryUi(session.status, session.openingTimeStamp, session.closingTimeStamp).copy(
                                dailyGoalAmount = session.dailyGoalAmount,
                                isLatestSession = latestSession?.id == session.id
                            )

                            // Populate Goals list dynamically
                            val dynamicGoals = mutableListOf<GoalUi>()
                            if (session.dailyGoalAmount != null && session.dailyGoalAmount > 0) {
                                val currentInflow = summaryUi.totalInflowValue
                                // progress formula (0.0 to 1.0)
                                val progress = (currentInflow.toFloat() / session.dailyGoalAmount.toFloat()).coerceIn(0f, 1f)
                                dynamicGoals.add(
                                    GoalUi(
                                        title = "Meta diária",
                                        currentFormatted = currentInflow.toCurrencyString(),
                                        targetFormatted = session.dailyGoalAmount.toCurrencyString(),
                                        progress = progress,
                                        icon = Icons.Default.Flag,
                                        isAchieved = currentInflow >= session.dailyGoalAmount
                                    )
                                )
                            }

                            // TODO: Later on we can add other goals here

                            CashDetailUiState(
                                session = session.toUiModel(),
                                summary = summaryUi,
                                historyItems = items,
                                categoryBalances = mockCategoryBalances,
                                paymentMethodBalances = mockPaymentMethodBalances,
                                goals = dynamicGoals,
                                transactionDetails = detailsMap,
                                selectedTransaction = _uiState.value.selectedTransaction,
                                isLoading = false
                            )
                        }
                    }
                }
                .collect { newState ->
                    _uiState.value = newState
                }
        }
    }

    fun updateDailyGoal(newAmount: Long) {
        viewModelScope.launch {
            val goalToSave = if (newAmount > 0) newAmount else null
            cashRepository.updateDailyGoal(cashId, goalToSave)
            // Flow from getSessionById will automatically refresh the UI state
        }
    }

    fun selectTransaction(id: String) {
        val details = _uiState.value.transactionDetails[id]
        if (details != null) {
            _uiState.value = _uiState.value.copy(selectedTransaction = details)
        }
    }

    fun clearSelectedTransaction() {
        _uiState.value = _uiState.value.copy(selectedTransaction = null)
    }
}
