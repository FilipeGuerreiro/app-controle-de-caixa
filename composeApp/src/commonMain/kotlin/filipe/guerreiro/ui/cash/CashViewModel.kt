package filipe.guerreiro.ui.cash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.domain.model.toCurrencyString
import filipe.guerreiro.domain.repository.CashRepository
import filipe.guerreiro.domain.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Inventory2
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.combine

data class CashSessionUi(
    val id: String,
    val date: String,
    val status: String, // "Aberto", "Fechado"
    val initialBalance: String,
    val finalBalance: String? = null,
    val isCurrent: Boolean = false
)

data class HistoryItemUi(
    val id: String,
    val title: String,
    val time: String,
    val method: String,
    val amountLabel: String,
    val isIncome: Boolean,
    val icon: ImageVector
)

data class CashUiState(
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean? = null,
    val cashSessions: List<CashSessionUi> = emptyList(),
    val selectedSession: CashSessionUi? = null,
    val historyItems: List<HistoryItemUi> = emptyList()
)

class CashViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val repository: CashRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val cashIdArg: String? = savedStateHandle.get<String>("cashId")
    private val cashId: Long? = cashIdArg?.toLongOrNull()?.takeIf { it != -1L }

    private val _modeState = MutableStateFlow<CashScreenMode>(
        if (cashId != null) CashScreenMode.Detail(cashId) else CashScreenMode.List
    )

    val uiState: StateFlow<CashUiState> =
        _modeState.flatMapLatest { mode ->
            sessionManager.currentUser.flatMapLatest { user ->
                if (user == null) {
                    flowOf(CashUiState(isLoading = false, isLoggedIn = false))
                } else {
                    when (mode) {
                        is CashScreenMode.List -> {
                            repository.getAllSessions(user.id)
                                .map { sessions ->
                                    val cashSessionUiList = sessions.map { session ->
                                        CashSessionUi(
                                            id = session.id.toString(),
                                            date = session.openingTimeStamp.toString(), // TODO: Format date
                                            status = session.status.name,
                                            initialBalance = session.initialAmount.toCurrencyString(),
                                            isCurrent = session.status == filipe.guerreiro.domain.model.CashStatusType.OPEN
                                        )
                                    }

                                    CashUiState(
                                        isLoading = false,
                                        isLoggedIn = true,
                                        cashSessions = cashSessionUiList,
                                        selectedSession = null
                                    )
                                }
                        }
                        is CashScreenMode.Detail -> {
                             combine(
                                 repository.getSessionById(mode.cashId),
                                 repository.getRecentTransactions(mode.cashId, 50)
                             ) { session, transactions ->
                                    if (session != null) {
                                        val sessionUi = CashSessionUi(
                                            id = session.id.toString(),
                                            date = session.openingTimeStamp.toString(), // TODO: Format date
                                            status = session.status.name,
                                            initialBalance = session.initialAmount.toCurrencyString(),
                                            isCurrent = session.status == filipe.guerreiro.domain.model.CashStatusType.OPEN
                                        )
                                        
                                        val history = transactions.map { tx ->
                                            val isIncome = tx.type == filipe.guerreiro.domain.model.TransactionType.INCOME
                                            HistoryItemUi(
                                                id = tx.id.toString(),
                                                title = tx.description,
                                                time = tx.timestamp.toString(), // TODO: Format time
                                                method = tx.type.name,
                                                amountLabel = (if(isIncome) "+ " else "- ") + tx.amount.toCurrencyString(),
                                                isIncome = isIncome,
                                                icon = if(isIncome) Icons.Default.LocalDining else Icons.Default.Inventory2 // TODO: Map icon based on category
                                            )
                                        }

                                        CashUiState(
                                            isLoading = false,
                                            isLoggedIn = true,
                                            selectedSession = sessionUi,
                                            historyItems = history
                                        )
                                    } else {
                                        CashUiState(
                                            isLoading = false,
                                            isLoggedIn = true
                                        )
                                    }
                             }
                        }
                    }
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            CashUiState()
        )


    fun selectSession(session: CashSessionUi) {
        // In List mode, clicking a session should switch to Detail mode or just set selectedSession?
        // If we switch mode, we re-fetch. If we just set selectedSession, we need to ensure we have the data.
        // For now let's update mode to Detail.
        val id = session.id.toLongOrNull()
        if (id != null) {
            _modeState.update { CashScreenMode.Detail(id) }
        }
    }
    
    fun selectSessionById(id: Long) {
         _modeState.update { CashScreenMode.Detail(id) }
    }

    fun clearSelection() {
        _modeState.update { CashScreenMode.List }
    }

    fun previousSession() {
        // TODO: Implement navigation between sessions
    }

    fun nextSession() {
        // TODO: Implement navigation between sessions
    }
}