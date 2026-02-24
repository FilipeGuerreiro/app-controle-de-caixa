package filipe.guerreiro.ui.opening

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.domain.model.toCurrencyStringWithoutPrefix
import filipe.guerreiro.domain.repository.CashRepository
import filipe.guerreiro.domain.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OpeningViewModel(
    private val repository: CashRepository,
    private val sessionManager: SessionManager,
    private val sessionPreferences: filipe.guerreiro.data.SessionPreferences
) : ViewModel() {

    private val inputState = MutableStateFlow(OpeningInputState())
    private val suggestedAmountState = MutableStateFlow<Long?>(null)
    private val isSessionOpenedState = MutableStateFlow(false)
    private val openedSessionIdState = MutableStateFlow<Long?>(null)
    private val isLoadingState = MutableStateFlow(false)

    init {
        loadSuggestedAmount()
    }

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<OpeningUiState> =
        combine(
            sessionManager.currentUser,
            inputState,
            suggestedAmountState,
            isSessionOpenedState,
            openedSessionIdState,
            isLoadingState
        ) { values ->
            val user = values[0] as? filipe.guerreiro.domain.model.User
            val input = values[1] as OpeningInputState
            val suggested = values[2] as Long?
            val opened = values[3] as Boolean
            val openedId = values[4] as Long?
            val loading = values[5] as Boolean

            OpeningUiState(
                suggestedAmount = suggested ?: 0L,
                displayAmount = input.amountInCents.toCurrencyStringWithoutPrefix(),
                amountInCents = input.amountInCents,
                displayDailyGoal = input.dailyGoalInCents.toCurrencyStringWithoutPrefix(),
                dailyGoalInCents = input.dailyGoalInCents,
                isLoading = loading,
                isSessionOpened = opened,
                openedSessionId = openedId,
                isLogged = user != null,
                errorMessage = input.errorMessage
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = OpeningUiState(isLoading = true)
        )

    private fun loadSuggestedAmount() {
        val user = sessionManager.currentUser.value
        if (user == null) {
            isLoadingState.value = false
            return
        }

        viewModelScope.launch {
            isLoadingState.value = true
            val suggested = repository.getSuggestedInitialAmount(user.id)
            val defaultGoal = sessionPreferences.getDefaultDailyGoal() ?: 0L
            suggestedAmountState.value = suggested

            // Pré-preenche o input com o valor sugerido e a meta padrão
            inputState.update {
                it.copy(
                    amountInCents = if (suggested > 0) suggested else 0L,
                    dailyGoalInCents = defaultGoal
                )
            }

            isLoadingState.value = false
        }
    }

    /**
     * Recebe o texto bruto do campo de input inicial, filtra apenas dígitos
     * e converte para centavos.
     */
    fun onAmountChange(newText: String) {
        val digitsOnly = newText.filter { it.isDigit() }
        val cents = digitsOnly.toLongOrNull() ?: 0L
        val capped = cents.coerceIn(0L, 99999999L) // Max R$ 999.999,99
        inputState.update {
            it.copy(amountInCents = capped, errorMessage = null)
        }
    }

    /**
     * Recebe o texto bruto do campo de input da meta, filtra apenas dígitos
     * e converte para centavos.
     */
    fun onDailyGoalChange(newText: String) {
        val digitsOnly = newText.filter { it.isDigit() }
        val cents = digitsOnly.toLongOrNull() ?: 0L
        val capped = cents.coerceIn(0L, 99999999L) // Max R$ 999.999,99
        inputState.update {
            it.copy(dailyGoalInCents = capped, errorMessage = null)
        }
    }

    fun openSession() {
        val user = sessionManager.currentUser.value ?: return
        val amountInCents = uiState.value.amountInCents
        val dailyGoalInCents = uiState.value.dailyGoalInCents

        viewModelScope.launch {
            try {
                sessionPreferences.setDefaultDailyGoal(dailyGoalInCents)
                val newSessionId = repository.createSession(amountInCents, user.id, if (dailyGoalInCents > 0) dailyGoalInCents else null)
                openedSessionIdState.value = newSessionId
                isSessionOpenedState.value = true
            } catch (e: Exception) {
                inputState.update {
                    it.copy(errorMessage = "Erro ao abrir o caixa, ${e.message}")
                }
            }
        }
    }

    fun resetToSuggestedAmount() {
        val suggested = suggestedAmountState.value ?: return
        inputState.update {
            it.copy(amountInCents = suggested)
        }
    }
}