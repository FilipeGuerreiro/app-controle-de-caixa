package filipe.guerreiro.ui.cash.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.domain.model.toCurrencyString
import filipe.guerreiro.domain.model.toUiModel
import filipe.guerreiro.domain.repository.CashRepository
import filipe.guerreiro.domain.repository.TransactionRepository
import filipe.guerreiro.domain.session.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

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
    val initialAmountValue: Long = 0L,
    val transactionCount: Int = 0,
    val isCurrent: Boolean,
    val timestamp: Long = 0L,
    val balanceValue: Long? = null
)

class CashListViewModel(
    private val cashRepository: CashRepository,
    private val sessionManager: SessionManager,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _filters = MutableStateFlow(CashListFilters())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CashListUiState> =
        combine(
            sessionManager.currentUser.filterNotNull().flatMapLatest { user ->
                observeSessionsForUser(user.id)
            },
            _filters
        ) { sessions, filters ->
            CashListUiState(
                sessions = sessions,
                filteredSessions = applyFilters(sessions, filters),
                isLoading = false,
                error = null,
                filters = filters
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CashListUiState(isLoading = true)
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeSessionsForUser(userId: Long): Flow<List<CashSessionUi>> {
        return cashRepository.getAllSessions(userId)
            .flatMapLatest { sessions ->
                if (sessions.isEmpty()) {
                    flowOf(emptyList())
                } else {
                    combine(
                        sessions.map { session ->
                            combine(
                                cashRepository.getSessionBalance(session.id),
                                transactionRepository.getAllTransactions(session.id)
                            ) { balance, transactions ->
                                val base = session.toUiModel()
                                base.copy(
                                    finalBalance = balance.currentBalance.toCurrencyString(),
                                    initialAmount = balance.initial.toCurrencyString(),
                                    initialAmountValue = balance.initial,
                                    transactionCount = transactions.size,
                                    balanceValue = balance.currentBalance
                                )
                            }
                        }
                    ) { perSession ->
                        perSession.toList()
                    }.map { list ->
                        list.sortedByDescending { it.timestamp }
                    }
                }
            }
    }







    fun updateFilters(newFilters: CashListFilters) {
        _filters.value = newFilters
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
