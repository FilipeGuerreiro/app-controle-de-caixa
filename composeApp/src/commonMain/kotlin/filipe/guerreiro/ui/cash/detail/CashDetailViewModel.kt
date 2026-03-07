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
import kotlinx.datetime.TimeZone

data class CashDetailUiState(
    val session: CashSessionUi? = null,
    val summary: CashSummaryUi = CashSummaryUi(),
    val historyItems: List<HistoryItemUi> = emptyList(),
    val categoryBalances: List<BreakdownItemUi> = emptyList(),
    val paymentMethodBalances: List<BreakdownItemUi> = emptyList(),
    val goals: List<GoalUi> = emptyList(),
    val transactionDetails: Map<String, TransactionDetailUi> = emptyMap(),
    val selectedTransaction: TransactionDetailUi? = null,
    val auditLogs: List<filipe.guerreiro.domain.model.AuditLog> = emptyList(),
    val rawTransactions: List<filipe.guerreiro.domain.model.Transaction> = emptyList(),
    val showAuditFrictionDialog: Boolean = false,
    val pendingTransactionAction: PendingTransactionAction? = null,

    val isLoading: Boolean = true
)

sealed class PendingTransactionAction {
    data class Edit(val oldTransaction: filipe.guerreiro.domain.model.Transaction, val newTransaction: filipe.guerreiro.domain.model.Transaction) : PendingTransactionAction()
    data class Delete(val transaction: filipe.guerreiro.domain.model.Transaction) : PendingTransactionAction()
}

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
    private val paymentMethodRepository: filipe.guerreiro.domain.repository.PaymentMethodRepository,
    private val updateTransactionUseCase: filipe.guerreiro.domain.usecase.UpdateTransactionUseCase,
    private val deleteTransactionUseCase: filipe.guerreiro.domain.usecase.DeleteTransactionUseCase,
    private val getSessionAuditLogsUseCase: filipe.guerreiro.domain.usecase.GetSessionAuditLogsUseCase
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
                        val flowA = combine(
                            transactionRepository.getAllTransactions(cashId),
                            cashRepository.getSessionBalance(cashId),
                            categoryRepository.getCategories(session.userId)
                        ) { txs, bal, cats -> Triple(txs, bal, cats) }

                        val flowB = combine(
                            paymentMethodRepository.getPaymentMethods(session.userId),
                            cashRepository.getCurrentCashSession(session.userId),
                            getSessionAuditLogsUseCase(cashId)
                        ) { pms, latestSession, logs -> Triple(pms, latestSession, logs) }

                        combine(flowA, flowB) { tripleA, tripleB ->
                            val transactions = tripleA.first
                            val balance = tripleA.second
                            val categories = tripleA.third
                            val paymentMethods = tripleB.first
                            val latestSession = tripleB.second
                            val auditLogs = tripleB.third
                        val mappedTransactions = transactions.map { tx ->
                            val category = categories.find { it.id == tx.categoryId }
                            val paymentMethod = paymentMethods.find { it.id == tx.paymentMethodId }
                            val title = listOfNotNull(category?.name, paymentMethod?.name).joinToString(" • ")

                            val isIncome = tx.type.name == filipe.guerreiro.domain.model.TransactionType.INCOME.name
                            val local = tx.timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
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

                            val summaryUi = balance.toSummaryUi(session.status, session.openingTimeStamp, session.closingTimeStamp).copy(
                                dailyGoalAmount = session.dailyGoalAmount,
                                isLatestSession = latestSession?.id == session.id
                            )

                            val totalInflow = summaryUi.totalInflowValue
                            val totalOutflow = summaryUi.totalOutflowValue

                            val realCategoryBalances = transactions.groupBy { Pair(it.categoryId, it.type) }
                                .map { (key, txs) ->
                                    val (categoryId, type) = key
                                    val isIncome = type.name == filipe.guerreiro.domain.model.TransactionType.INCOME.name
                                    val totalAmount = txs.sumOf { it.amount }
                                    val categoryName = categories.find { it.id == categoryId }?.name ?: "Sem Categoria"
                                    
                                    val totalOfType = if (isIncome) totalInflow else totalOutflow
                                    val progress = if (totalOfType > 0) totalAmount.toFloat() / totalOfType.toFloat() else 0f

                                    BreakdownItemUi(
                                        name = categoryName,
                                        amountFormatted = totalAmount.toCurrencyString(),
                                        amountValue = totalAmount,
                                        isIncome = isIncome,
                                        progress = progress
                                    )
                                }.sortedByDescending { it.amountValue }

                            val realPaymentMethodBalances = transactions.groupBy { Pair(it.paymentMethodId, it.type) }
                                .map { (key, txs) ->
                                    val (paymentMethodId, type) = key
                                    val isIncome = type.name == filipe.guerreiro.domain.model.TransactionType.INCOME.name
                                    val totalAmount = txs.sumOf { it.amount }
                                    val paymentMethodName = paymentMethods.find { it.id == paymentMethodId }?.name ?: "Sem Método"
                                    
                                    val totalOfType = if (isIncome) totalInflow else totalOutflow
                                    val progress = if (totalOfType > 0) totalAmount.toFloat() / totalOfType.toFloat() else 0f

                                    BreakdownItemUi(
                                        name = paymentMethodName,
                                        amountFormatted = totalAmount.toCurrencyString(),
                                        amountValue = totalAmount,
                                        isIncome = isIncome,
                                        progress = progress
                                    )
                                }.sortedByDescending { it.amountValue }

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


                            CashDetailUiState(
                                session = session.toUiModel(),
                                summary = summaryUi,
                                historyItems = items,
                                categoryBalances = realCategoryBalances,
                                paymentMethodBalances = realPaymentMethodBalances,
                                goals = dynamicGoals,
                                transactionDetails = detailsMap,
                                selectedTransaction = _uiState.value.selectedTransaction,
                                auditLogs = auditLogs,
                                rawTransactions = transactions,
                                showAuditFrictionDialog = _uiState.value.showAuditFrictionDialog,
                                pendingTransactionAction = _uiState.value.pendingTransactionAction,
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

    fun requestTransactionDelete(transactionId: String) {
        val uiStateValue = _uiState.value
        val transaction = uiStateValue.rawTransactions.find { it.id.toString() == transactionId } ?: return
        val isClosed = uiStateValue.session?.status == "Fechado"

        if (isClosed) {
            _uiState.value = uiStateValue.copy(
                pendingTransactionAction = PendingTransactionAction.Delete(transaction),
                showAuditFrictionDialog = true
            )
        } else {
            viewModelScope.launch {
                deleteTransactionUseCase(transaction, wasClosed = false, reason = null)
                clearSelectedTransaction()
            }
        }
    }

    fun requestTransactionEdit(oldTransactionId: String, newAmount: Long, newCategoryId: Long, newDescription: String) {
        val uiStateValue = _uiState.value
        val oldTx = uiStateValue.rawTransactions.find { it.id.toString() == oldTransactionId } ?: return
        val newTx = oldTx.copy(
            amount = newAmount,
            categoryId = newCategoryId,
            description = newDescription
        )
        val isClosed = uiStateValue.session?.status == "Fechado"

        if (isClosed) {
            _uiState.value = uiStateValue.copy(
                pendingTransactionAction = PendingTransactionAction.Edit(oldTx, newTx),
                showAuditFrictionDialog = true
            )
        } else {
            viewModelScope.launch {
                updateTransactionUseCase(oldTx, newTx, wasClosed = false, reason = null)
                // Transaction is updated without audit tracking
            }
        }
    }

    fun confirmAuditAction(reason: String) {
        val state = _uiState.value
        val action = state.pendingTransactionAction ?: return

        viewModelScope.launch {
            when (action) {
                is PendingTransactionAction.Edit -> {
                    updateTransactionUseCase(
                        oldTransaction = action.oldTransaction,
                        newTransaction = action.newTransaction,
                        wasClosed = true,
                        reason = reason
                    )
                }
                is PendingTransactionAction.Delete -> {
                    deleteTransactionUseCase(
                        transaction = action.transaction,
                        wasClosed = true,
                        reason = reason
                    )
                    clearSelectedTransaction()
                }
            }
            dismissAuditFrictionDialog()
        }
    }

    fun dismissAuditFrictionDialog() {
        _uiState.value = _uiState.value.copy(
            showAuditFrictionDialog = false,
            pendingTransactionAction = null
        )
    }


}
