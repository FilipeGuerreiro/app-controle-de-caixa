package filipe.guerreiro.ui.cash.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.domain.model.CashSession
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
    val filteredSessions: List<CashSessionUi> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val filters: CashListFilters = CashListFilters()
)

data class CashListFilters(
    val minTransactions: Int? = null,
    val maxTransactions: Int? = null,
    // Note: Date filtering would ideally use specific types, but for simplicity we'll assume filtering logic
    // is done via String or simpler means for now, or if needed we can parse dates.
    // Given the context, we will filter by transaction count as requested and maybe date range if feasible.
    // For now, let's stick to transaction count as it's easier to implement without complex date pickers first.
    // The user asked for "intervalo de datas para abertura/fechamento" too.
    val startDate: Long? = null, // timestamp
    val endDate: Long? = null // timestamp
) {
    val isActive: Boolean
        get() = minTransactions != null || maxTransactions != null || startDate != null || endDate != null
}

data class CashSessionUi(
    val id: Long,
    val date: String,
    val status: String,
    val finalBalance: String?,
    val initialAmount: String = "R$ 0,00",
    val transactionCount: Int = 0,
    val isCurrent: Boolean,
    val timestamp: Long = 0L // Added for sorting/filtering
)

class CashListViewModel(
    private val cashRepository: CashRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(CashListUiState(isLoading = true))
    val uiState: StateFlow<CashListUiState> = _uiState.asStateFlow()

    private var originalSessions: List<CashSessionUi> = emptyList()

    init {
        loadSessions()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadSessions() {
        viewModelScope.launch {
            try {
                sessionManager.currentUser
                    .filterNotNull()
                    .flatMapLatest { user ->
                        cashRepository.getAllSessions(user.id)
                    }
                    .collect { sessions ->
                        val uiSessions = sessions.map { session -> session.toUiModel() }
                            .sortedByDescending { it.timestamp } // Ensure most recent first
                        
                        originalSessions = uiSessions
                        
                        _uiState.update {
                            it.copy(
                                sessions = uiSessions,
                                filteredSessions = applyFilters(uiSessions, it.filters),
                                isLoading = false
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Erro ao carregar caixas: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateFilters(newFilters: CashListFilters) {
        _uiState.update {
            it.copy(
                filters = newFilters,
                filteredSessions = applyFilters(originalSessions, newFilters)
            )
        }
    }

    fun clearFilters() {
        updateFilters(CashListFilters())
    }

    private fun applyFilters(sessions: List<CashSessionUi>, filters: CashListFilters): List<CashSessionUi> {
        if (!filters.isActive) return sessions

        return sessions.filter { session ->
            val matchesTransactions = (filters.minTransactions == null || session.transactionCount >= filters.minTransactions) &&
                                      (filters.maxTransactions == null || session.transactionCount <= filters.maxTransactions)
            
            val matchesDate = (filters.startDate == null || session.timestamp >= filters.startDate) &&
                              (filters.endDate == null || session.timestamp <= filters.endDate)

            matchesTransactions && matchesDate
        }
    }
}
