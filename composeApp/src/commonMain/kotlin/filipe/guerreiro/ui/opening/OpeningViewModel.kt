package filipe.guerreiro.ui.opening

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.domain.model.toCents
import filipe.guerreiro.domain.model.toCurrencyString
import filipe.guerreiro.domain.repository.CashRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class OpeningViewModel(
    private val repository: CashRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OpeningUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadSuggestedAmount()
    }

    private fun loadSuggestedAmount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val suggested = repository.getSuggestedInitialAmount()
            _uiState.update {
                it.copy(
                    suggestedAmount = suggested,
                    initialAmountInput = suggested.toCurrencyString(),
                    isLoading = false
                )
            }
        }
    }

    fun onAmountChange(newValue: String) {
        _uiState.update { it.copy(initialAmountInput = newValue) }
    }

    fun openSession() {
        val amountInCents = _uiState.value.initialAmountInput.toCents()
        viewModelScope.launch {
            try {
                repository.createSession(amountInCents)
                _uiState.update { it.copy(isSessionOpened = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Erro ao abrir caixa") }
            }
        }
    }

    fun resetToSuggestedAmount() {
        val suggested = _uiState.value.suggestedAmount
        _uiState.update {
            it.copy(initialAmountInput = suggested.toCurrencyString())
        }
    }
}