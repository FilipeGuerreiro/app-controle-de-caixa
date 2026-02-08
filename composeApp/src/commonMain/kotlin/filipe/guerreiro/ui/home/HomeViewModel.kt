package filipe.guerreiro.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.domain.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecentActivity(
    val id: String,
    val title: String,
    val time: String,
    val type: String,
    val amount: Double,
    val isIncome: Boolean,
    val icon: ImageVector
)

data class QuickActionUiModel(
    val id: Int,
    val emoji: String,
    val title: String,
    val priceStr: String
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean? = null, // null = ainda verificando, false = não logado, true = logado
    val userName: String = "",
    val businessName: String = "",
    val quickActions: List<QuickActionUiModel> = emptyList(),
    val recentActivities: List<RecentActivity> = emptyList(),
    val isCashOpen: Boolean = false,
    val dailyGoal: String = "",
    val dailyProgress: Int = 0,
    val totalIncome: String = "",
    val totalExpense: String = ""
)

class HomeViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            // Observa mudanças no usuário logado
            sessionManager.currentUser.collect { user ->
                if (user != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            userName = user.name,
                            businessName = user.businessName,
                            // TODO: Carregar do banco de dados
                            quickActions = emptyList(),
                            recentActivities = emptyList(),
                            isCashOpen = false,
                            dailyGoal = "",
                            dailyProgress = 0,
                            totalIncome = "",
                            totalExpense = ""
                        )
                    }
                } else {
                    // Usuário não logado
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = false) }
                }
            }
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            sessionManager.refreshSession()
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.logout()
        }
    }
}