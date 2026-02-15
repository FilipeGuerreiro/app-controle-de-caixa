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
    private val sessionManager: SessionManager
) : ViewModel() {

    private val inputState = MutableStateFlow(OpeningInputState())
    private val suggestedAmountState = MutableStateFlow<Long?>(null)
    private val isSessionOpenedState = MutableStateFlow(false)
    private val isLoadingState = MutableStateFlow(false)

    init {
        loadSuggestedAmount()
    }

    val uiState: StateFlow<OpeningUiState> =
        combine(
            sessionManager.currentUser,
            inputState,
            suggestedAmountState,
            isSessionOpenedState,
            isLoadingState
        ) { user, input, suggested, opened, loading ->
            OpeningUiState(
                suggestedAmount = suggested ?: 0L,
                displayAmount = input.amountInCents.toCurrencyStringWithoutPrefix(),
                amountInCents = input.amountInCents,
                isLoading = loading,
                isSessionOpened = opened,
                isLogged = user != null,
                errorMessage = input.errorMessage
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = OpeningUiState(isLoading = true)
        )

    private fun loadSuggestedAmount() {
        val user = sessionManager.currentUser.value ?: return

        viewModelScope.launch {
            isLoadingState.value = true
            val suggested = repository.getSuggestedInitialAmount(user.id)
            println("suggested: $suggested")
            suggestedAmountState.value = suggested

            // Pré-preenche o input com o valor sugerido
            if (suggested != null && suggested > 0) {
                inputState.update {
                    it.copy(amountInCents = suggested)
                }
            }

            isLoadingState.value = false
        }
    }

    /**
     * Recebe o texto bruto do campo de input, filtra apenas dígitos
     * e converte para centavos. Funciona no estilo "ATM": os dígitos
     * preenchem da direita para a esquerda (ex: digitar "1" = R$ 0,01).
     */
    fun onAmountChange(newText: String) {
        val digitsOnly = newText.filter { it.isDigit() }
        val cents = digitsOnly.toLongOrNull() ?: 0L
        val capped = cents.coerceIn(0L, 99999999L) // Max R$ 999.999,99
        inputState.update {
            it.copy(amountInCents = capped, errorMessage = null)
        }
    }

    fun openSession() {
        val user = sessionManager.currentUser.value ?: return
        val amountInCents = uiState.value.amountInCents

        viewModelScope.launch {
            try {
                repository.createSession(amountInCents, user.id)
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