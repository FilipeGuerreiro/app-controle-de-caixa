package filipe.guerreiro.ui.cash.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.domain.model.toUiModel
import filipe.guerreiro.domain.repository.CashRepository
import filipe.guerreiro.domain.session.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CashListUiState(
    val sessions: List<CashSessionUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class CashSessionUi(
    val id: Long,
    val date: String,
    val status: String,
    val finalBalance: String?,
    val isCurrent: Boolean
)

class CashListViewModel(
    private val cashRepository: CashRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CashListUiState(isLoading = true))
    val uiState: StateFlow<CashListUiState> = _uiState.asStateFlow()

    init {
        loadSessions()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadSessions() {
        viewModelScope.launch {
            sessionManager.currentUser
                .filterNotNull()
                .flatMapLatest { user ->
                    cashRepository.getAllSessions(user.id)
                }
                .collect { sessions ->
                    _uiState.update {
                        it.copy(
                            sessions = sessions.map { session -> session.toUiModel() },
                            isLoading = false
                        )
                    }
                }
        }
    }

}