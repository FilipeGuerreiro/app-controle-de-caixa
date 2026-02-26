package filipe.guerreiro.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.domain.model.TransactionType
import filipe.guerreiro.domain.model.User
import filipe.guerreiro.domain.model.toCurrencyString
import filipe.guerreiro.domain.repository.CashRepository
import filipe.guerreiro.domain.repository.TransactionRepository
import filipe.guerreiro.domain.session.SessionManager
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

import filipe.guerreiro.domain.repository.CategoryRepository
import filipe.guerreiro.domain.repository.PaymentMethodRepository

data class QuickActionUiModel(
    val categoryId: Long,
    val paymentMethodId: Long,
    val type: TransactionType,
    val categoryName: String,
    val paymentMethodName: String
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean? = null,
    val userName: String = "",
    val businessName: String = "",
    val quickActions: List<QuickActionUiModel> = emptyList(),
    val averageTicket: Long = 0L,
    val salesCount: Int = 0,
    val isCashOpenForTooLong: Boolean = false,
    val isCashOpen: Boolean = false,
    val isFirstAccess: Boolean = false,
    val dailyGoalAmount: Long? = null,
    val totalIncomeValue: Long = 0L,
    val totalIncome: String = "",
    val totalExpense: String = "",
    val currentBalance: String = "R$ 0,00",
    val initialAmountValue: Long = 0L,
    val currentBalanceValue: Long = 0L,
    val currentCashId: Long? = null,
    val quickActionError: String? = null
)

class HomeViewModel(
    private val sessionManager: SessionManager,
    private val cashRepository: CashRepository,
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = sessionManager
        .currentUser
        .flatMapLatest { user ->
            if (user == null) {
                flowOf(
                    HomeUiState(
                        isLoading = false,
                        isLoggedIn = false
                    )
                )
            } else {
                observeCashForUser(user)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(isLoading = true)
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeCashForUser(user: User): Flow<HomeUiState> {
        val categoriesFlow = categoryRepository.getCategories(user.id)
        val paymentMethodsFlow = paymentMethodRepository.getPaymentMethods(user.id)

        val quickActionsFlow = combine(
            transactionRepository.getTopFrequentTransactions(user.id),
            categoriesFlow,
            paymentMethodsFlow
        ) { frequentTxs, categories, paymentMethods ->
            frequentTxs.mapNotNull { tx ->
                val category = categories.find { it.id == tx.categoryId }
                val paymentMethod = paymentMethods.find { it.id == tx.paymentMethodId }

                if (category != null && paymentMethod != null) {
                    QuickActionUiModel(
                        categoryId = tx.categoryId,
                        paymentMethodId = tx.paymentMethodId,
                        type = tx.type,
                        categoryName = category.name,
                        paymentMethodName = paymentMethod.name
                    )
                } else null
            }
        }

        return cashRepository
            .getCurrentCashSession(user.id)
            .flatMapLatest { cashSession ->
                if (cashSession == null) {
                    flowOf(
                        HomeUiState(
                            isLoading = false,
                            isLoggedIn = true,
                            isCashOpen = false,
                            isFirstAccess = true,
                            userName = user.name,
                            businessName = user.businessName,
                            totalIncome = 0L.toCurrencyString(),
                            totalExpense = 0L.toCurrencyString(),
                            currentBalance = 0L.toCurrencyString(),
                            initialAmountValue = 0L,
                            currentBalanceValue = 0L,
                            averageTicket = 0L,
                            salesCount = 0,
                            isCashOpenForTooLong = false,
                            quickActions = emptyList()
                        )
                    )
                } else {
                    combine(
                        cashRepository.getSessionBalance(cashSession.id),
                        transactionRepository.getAllTransactions(cashSession.id),
                        quickActionsFlow,
                        categoriesFlow,
                        paymentMethodsFlow
                    ) { balance, transactions, quickActions, categories, paymentMethods ->
                        val sales = transactions.filter { it.type == TransactionType.INCOME }
                        val salesCount = sales.size
                        val averageTicket = if (salesCount > 0) balance.totalIncomes / salesCount else 0L

                        val nowMs = kotlin.time.Clock.System.now().toEpochMilliseconds()
                        val openMs = cashSession.openingTimeStamp.toEpochMilliseconds()
                        val diffMs = nowMs - openMs
                        val isTooLong = cashSession.status == filipe.guerreiro.domain.model.CashStatusType.OPEN && diffMs > 86400000L

                        HomeUiState(
                            isLoading = false,
                            isLoggedIn = true,
                            isCashOpen = cashSession.status == filipe.guerreiro.domain.model.CashStatusType.OPEN,
                            isFirstAccess = false,
                            userName = user.name,
                            businessName = user.businessName,
                            totalIncome = balance.totalIncomes.toCurrencyString(),
                            totalExpense = balance.totalExpenses.toCurrencyString(),
                            currentBalance = balance.currentBalance.toCurrencyString(),
                            initialAmountValue = cashSession.initialAmount,
                            currentBalanceValue = balance.currentBalance,
                            dailyGoalAmount = cashSession.dailyGoalAmount,
                            totalIncomeValue = balance.totalIncomes,
                            averageTicket = averageTicket,
                            salesCount = salesCount,
                            isCashOpenForTooLong = isTooLong,
                            quickActions = quickActions,
                            currentCashId = cashSession.id
                        )
                    }
                }
            }
    }

    fun addQuickTransaction(
        categoryId: Long,
        paymentMethodId: Long,
        type: TransactionType,
        amount: Long,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val user = sessionManager.currentUser.value ?: return@launch
            try {
                transactionRepository.addTransactionForUserCurrentSession(
                    userId = user.id,
                    categoryId = categoryId,
                    paymentMethodId = paymentMethodId,
                    amount = amount,
                    description = "Lançamento Rápido",
                    type = type
                )
                onSuccess()
            } catch (e: Exception) {
                // Handle error? uiState could have error field
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.logout()
        }
    }
}
