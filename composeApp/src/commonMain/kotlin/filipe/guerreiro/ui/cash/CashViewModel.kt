package filipe.guerreiro.ui.cash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.domain.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CashUiState(
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean? = null
)

class CashViewModel(
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CashUiState())
    val uiState: StateFlow<CashUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUser()
    }

    private fun observeCurrentUser() {
        viewModelScope.launch {
            sessionManager.currentUser.collect { user ->
                if (user != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, isLoggedIn = false) }
                }
            }
        }
    }
}