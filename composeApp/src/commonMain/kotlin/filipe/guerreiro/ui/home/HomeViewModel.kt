package filipe.guerreiro.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.domain.model.CashStatusType
import filipe.guerreiro.domain.model.TransactionType
import filipe.guerreiro.domain.model.User
import filipe.guerreiro.domain.model.toCurrencyString
import filipe.guerreiro.domain.model.toRecentActivity
import filipe.guerreiro.domain.repository.CashRepository
import filipe.guerreiro.domain.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
    val id: Long,
    val emoji: String,
    val title: String,
    val priceStr: String
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
    val currentCashId: Long? = null
)

class HomeViewModel(
    private val sessionManager: SessionManager,
    private val cashRepository: CashRepository
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

    private fun observeCashForUser(user: User): Flow<HomeUiState> {
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
                            recentActivities = emptyList(),
                            quickActions = emptyList()
                        )
                    )
                } else {
                    combine(
                        cashRepository.getSessionBalance(cashSession.id),
                        cashRepository.getRecentTransactions(cashSession.id, 5)
                    ) {
                        balance, transactions ->
                        HomeUiState(
                            isLoading = false,
                            isLoggedIn = true,
                            isCashOpen = cashSession.status == CashStatusType.OPEN,
                            isFirstAccess = false,
                            userName = user.name,
                            businessName = user.businessName,
                            totalIncome = balance.totalIncomes.toCurrencyString(),
                            totalExpense = balance.totalExpenses.toCurrencyString(),
                            recentActivities = transactions.map { tx ->
                                tx.toRecentActivity()
                            },
                            quickActions = emptyList(),
                            currentCashId = cashSession.id
                        )
                    }
                }
            }
    }


    fun logout() {
        viewModelScope.launch {
            sessionManager.logout()
        }
    }
}
