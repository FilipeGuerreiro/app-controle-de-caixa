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

enum class CashTabFilter {
    ALL, PROFIT, LOSS
}

data class CashListUiState(
    val sessions: List<CashSessionUi> = emptyList(),
    val currentSession: CashSessionUi? = null,
    val groupedSessions: Map<String, List<CashSessionUi>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedDateMillis: Long? = null,
    val activeFilterTab: CashTabFilter = CashTabFilter.ALL
)

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
    private val _selectedDateMillis = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _activeFilterTab = MutableStateFlow(CashTabFilter.ALL)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CashListUiState> =
        combine(
            sessionManager.currentUser.filterNotNull().flatMapLatest { user ->
                observeSessionsForUser(user.id)
            },
            _selectedDateMillis,
            _activeFilterTab
        ) { sessions, dateMillis, tab ->
            val (current, grouped) = processSessions(sessions, dateMillis, tab)

            CashListUiState(
                sessions = sessions,
                currentSession = current,
                groupedSessions = grouped,
                isLoading = false,
                error = null,
                selectedDateMillis = dateMillis,
                activeFilterTab = tab
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







    fun updateSelectedDate(dateMillis: Long?) {
        _selectedDateMillis.value = dateMillis
    }

    fun updateActiveFilterTab(tab: CashTabFilter) {
        _activeFilterTab.value = tab
    }

    fun clearFilters() {
        _selectedDateMillis.value = null
        _activeFilterTab.value = CashTabFilter.ALL
    }

    private fun processSessions(
        sessions: List<CashSessionUi>,
        dateMillis: Long?,
        tab: CashTabFilter
    ): Pair<CashSessionUi?, Map<String, List<CashSessionUi>>> {
        val currentSession = sessions.firstOrNull { it.isCurrent }
        val historySessions = sessions.filter { !it.isCurrent }

        val filtered = historySessions.filter { session ->
            // Date filter: compare day-level (same calendar day)
            val matchesDate = if (dateMillis != null) {
                val selectedDay = dateMillis / 86400000L
                val sessionDay = session.timestamp / 86400000L
                selectedDay == sessionDay
            } else true

            val matchesTab = when (tab) {
                CashTabFilter.ALL -> true
                CashTabFilter.PROFIT -> session.balanceValue != null && session.balanceValue > 0
                CashTabFilter.LOSS -> session.balanceValue != null && session.balanceValue < 0
            }

            matchesDate && matchesTab
        }

        val grouped = filtered.groupBy { session ->
            val parts = session.date.split("/", " ", "-")
            if (parts.size >= 3) {
                val month = parts[1]
                val year = parts[2].take(4)
                val monthName = when(month) {
                    "1" -> "Janeiro" "2" -> "Fevereiro" "3" -> "Março" "4" -> "Abril"
                    "5" -> "Maio" "6" -> "Junho" "7" -> "Julho" "8" -> "Agosto"
                    "9" -> "Setembro" "10" -> "Outubro" "11" -> "Novembro" "12" -> "Dezembro"
                    else -> month
                }
                "$monthName, $year"
            } else {
                "Sessões Anteriores"
            }
        }

        return Pair(currentSession, grouped)
    }
}
