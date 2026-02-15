package filipe.guerreiro.ui.closing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.domain.repository.CashRepository
import filipe.guerreiro.domain.session.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ClosingViewModel(
    private val repository: CashRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val isLoadingState = MutableStateFlow(false)
    private val isSessionClosedState = MutableStateFlow(false)

    private val errorState = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val closingDataFlow: Flow<ClosingDataState> =
        sessionManager.currentUser.flatMapLatest { user ->
            if (user == null) return@flatMapLatest flowOf(ClosingDataState())

            repository.getCurrentCashSession(user.id)
                .flatMapLatest { session ->
                    if (session == null) return@flatMapLatest flowOf(ClosingDataState())

                    repository.getSessionBalance(session.id).map {
                        balance ->
                        ClosingDataState(
                            initialBalance = balance.initial,
                            finalBalance = balance.currentBalance,
                            incomeBalance = balance.totalIncomes,
                            expenseBalance = balance.totalExpenses
                        )
                    }
                }
        }

    val uiState: StateFlow<ClosingUiState> =
        combine(
            sessionManager.currentUser,
            isLoadingState,
            isSessionClosedState,
            closingDataFlow,
            errorState
        ) {
            user, loading, closed, data, error ->
            ClosingUiState(
                isLoading = loading,
                isSessionClosed = closed,
                isLogged = user != null,
                closingData = data,
                errorMessage = error
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ClosingUiState(isLoading = true)
        )

    fun closeSession() {
        val user = sessionManager.currentUser.value ?: return

        viewModelScope.launch {
            try {
                isLoadingState.value = true
                repository.closeSession(user.id)
                isLoadingState.value = false
                isSessionClosedState.value = true
            } catch (e: Exception) {
                errorState.value = e.message ?: "Erro ao fechar caixa."
            }
        }
    }


}