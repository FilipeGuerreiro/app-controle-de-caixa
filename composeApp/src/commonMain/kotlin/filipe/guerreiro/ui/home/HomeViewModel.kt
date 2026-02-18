package filipe.guerreiro.ui.home

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.domain.model.CashStatusType
import filipe.guerreiro.domain.model.TransactionType
import filipe.guerreiro.domain.model.User
import filipe.guerreiro.domain.model.toCurrencyString
import filipe.guerreiro.domain.model.toRecentActivity
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

data class RecentActivity(
    val id: String,
    val title: String,
    val time: String,
    val type: String,
    val amount: Long,
    val isIncome: Boolean,
    val icon: ImageVector
)

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
    val recentActivities: List<RecentActivity> = emptyList(),
    val isCashOpen: Boolean = false,
    val isFirstAccess: Boolean = false, // true quando usuário nunca criou um caixa
    val dailyGoal: String = "R$ 500,00", // Mockado por enquanto
    val dailyProgress: Int = 0,
    val totalIncome: String = "",
    val totalExpense: String = "",
    val currentBalance: String = "R$ 0,00",
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
                            recentActivities = emptyList(),
                            quickActions = emptyList()
                        )
                    )
                } else {
                    combine(
                        cashRepository.getSessionBalance(cashSession.id),
                        transactionRepository.getRecentTransactions(cashSession.id, 5),
                        quickActionsFlow,
                        categoriesFlow,
                        paymentMethodsFlow
                    ) { balance, transactions, quickActions, categories, paymentMethods ->
                        HomeUiState(
                            isLoading = false,
                            isLoggedIn = true,
                            isCashOpen = cashSession.status == CashStatusType.OPEN,
                            isFirstAccess = false,
                            userName = user.name,
                            businessName = user.businessName,
                            totalIncome = balance.totalIncomes.toCurrencyString(),
                            totalExpense = balance.totalExpenses.toCurrencyString(),
                            currentBalance = balance.currentBalance.toCurrencyString(),
                            recentActivities = transactions.map { tx ->
                                val base = tx.toRecentActivity()
                                val category = categories.find { it.id == tx.categoryId }
                                val paymentMethod = paymentMethods.find { it.id == tx.paymentMethodId }

                                val title = when {
                                    category != null && paymentMethod != null ->
                                        "${category.name} • ${paymentMethod.name}"
                                    category != null -> category.name
                                    paymentMethod != null -> paymentMethod.name
                                    else -> base.title
                                }

                                base.copy(title = title)
                            },
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
