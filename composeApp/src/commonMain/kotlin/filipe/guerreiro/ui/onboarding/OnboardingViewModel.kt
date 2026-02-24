package filipe.guerreiro.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.domain.model.TransactionType
import filipe.guerreiro.domain.repository.CategoryRepository
import filipe.guerreiro.domain.repository.PaymentMethodRepository
import filipe.guerreiro.domain.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── Sugestão de item (antes de persistir) ──────────────────────────────────
data class SuggestedCategory(
    val name: String,
    val type: TransactionType,
    val isSelected: Boolean = false,
    val isCustom: Boolean = false
)

data class SuggestedPaymentMethod(
    val name: String,
    val isSelected: Boolean = false,
    val isCustom: Boolean = false
)

// ── UI State ────────────────────────────────────────────────────────────────
data class OnboardingUiState(
    val incomeCategories: List<SuggestedCategory> = emptyList(),
    val expenseCategories: List<SuggestedCategory> = emptyList(),
    val paymentMethods: List<SuggestedPaymentMethod> = emptyList(),
    val isSaving: Boolean = false,
    val showCustomCategoryDialog: Boolean = false,
    val customCategoryType: TransactionType = TransactionType.INCOME,
    val customCategoryName: String = "",
    val customCategoryError: String? = null,
    val showCustomPaymentDialog: Boolean = false,
    val customPaymentName: String = "",
    val customPaymentError: String? = null,
)

// ── ViewModel ───────────────────────────────────────────────────────────────
class OnboardingViewModel(
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        OnboardingUiState(
            incomeCategories = defaultIncomeCategories(),
            expenseCategories = defaultExpenseCategories(),
            paymentMethods = defaultPaymentMethods(),
        )
    )
    val uiState: StateFlow<OnboardingUiState> = _uiState

    // ── Categorias ──────────────────────────────────────────────────────

    fun toggleCategory(name: String, type: TransactionType) {
        _uiState.update { state ->
            when (type) {
                TransactionType.INCOME -> state.copy(
                    incomeCategories = state.incomeCategories.map {
                        if (it.name == name) it.copy(isSelected = !it.isSelected) else it
                    }
                )
                TransactionType.EXPENSE -> state.copy(
                    expenseCategories = state.expenseCategories.map {
                        if (it.name == name) it.copy(isSelected = !it.isSelected) else it
                    }
                )
            }
        }
    }

    fun onShowCustomCategoryDialog(type: TransactionType) {
        _uiState.update {
            it.copy(
                showCustomCategoryDialog = true,
                customCategoryType = type,
                customCategoryName = "",
                customCategoryError = null
            )
        }
    }

    fun onDismissCustomCategoryDialog() {
        _uiState.update { it.copy(showCustomCategoryDialog = false) }
    }

    fun onCustomCategoryNameChange(value: String) {
        _uiState.update { it.copy(customCategoryName = value, customCategoryError = null) }
    }

    fun onConfirmCustomCategory() {
        val state = _uiState.value
        val name = state.customCategoryName.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(customCategoryError = "O nome não pode ser vazio") }
            return
        }

        // Verificar duplicatas (case-insensitive) em ambas as listas
        val allCategories = state.incomeCategories + state.expenseCategories
        val isDuplicate = allCategories.any { it.name.equals(name, ignoreCase = true) }
        if (isDuplicate) {
            _uiState.update { it.copy(customCategoryError = "Já existe uma categoria com esse nome") }
            return
        }

        val type = state.customCategoryType
        val newItem = SuggestedCategory(name = name, type = type, isSelected = true, isCustom = true)

        _uiState.update {
            when (type) {
                TransactionType.INCOME -> it.copy(
                    incomeCategories = it.incomeCategories + newItem,
                    showCustomCategoryDialog = false
                )
                TransactionType.EXPENSE -> it.copy(
                    expenseCategories = it.expenseCategories + newItem,
                    showCustomCategoryDialog = false
                )
            }
        }
    }

    /** Persiste categorias selecionadas e invoca callback de sucesso. */
    fun saveCategories(onComplete: () -> Unit) {
        val state = _uiState.value
        val selected = state.incomeCategories.filter { it.isSelected } +
                state.expenseCategories.filter { it.isSelected }

        if (selected.isEmpty()) {
            onComplete()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val userId = sessionManager.currentUser.value?.id ?: return@launch
                selected.forEach { cat ->
                    categoryRepository.createCategory(userId, cat.name, cat.type)
                }
            } catch (_: Exception) {
                // silently continue – user can manage categories later
            } finally {
                _uiState.update { it.copy(isSaving = false) }
                onComplete()
            }
        }
    }

    // ── Métodos de Pagamento ─────────────────────────────────────────────

    fun togglePaymentMethod(name: String) {
        _uiState.update { state ->
            state.copy(
                paymentMethods = state.paymentMethods.map {
                    if (it.name == name) it.copy(isSelected = !it.isSelected) else it
                }
            )
        }
    }

    fun onShowCustomPaymentDialog() {
        _uiState.update {
            it.copy(showCustomPaymentDialog = true, customPaymentName = "", customPaymentError = null)
        }
    }

    fun onDismissCustomPaymentDialog() {
        _uiState.update { it.copy(showCustomPaymentDialog = false) }
    }

    fun onCustomPaymentNameChange(value: String) {
        _uiState.update { it.copy(customPaymentName = value, customPaymentError = null) }
    }

    fun onConfirmCustomPayment() {
        val state = _uiState.value
        val name = state.customPaymentName.trim()
        if (name.isBlank()) {
            _uiState.update { it.copy(customPaymentError = "O nome não pode ser vazio") }
            return
        }

        // Verificar duplicatas (case-insensitive)
        val isDuplicate = state.paymentMethods.any { it.name.equals(name, ignoreCase = true) }
        if (isDuplicate) {
            _uiState.update { it.copy(customPaymentError = "Já existe um método com esse nome") }
            return
        }

        _uiState.update {
            it.copy(
                paymentMethods = it.paymentMethods + SuggestedPaymentMethod(
                    name = name, isSelected = true, isCustom = true
                ),
                showCustomPaymentDialog = false
            )
        }
    }

    /** Persiste métodos selecionados e invoca callback de sucesso. */
    fun savePaymentMethods(onComplete: () -> Unit) {
        val selected = _uiState.value.paymentMethods.filter { it.isSelected }

        if (selected.isEmpty()) {
            onComplete()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val userId = sessionManager.currentUser.value?.id ?: return@launch
                selected.forEach { pm ->
                    paymentMethodRepository.createPaymentMethod(userId, pm.name)
                }
            } catch (_: Exception) {
                // silently continue
            } finally {
                _uiState.update { it.copy(isSaving = false) }
                onComplete()
            }
        }
    }

    // ── Defaults ─────────────────────────────────────────────────────────

    companion object {
        fun defaultIncomeCategories() = listOf(
            "Comidas",
            "Bebidas",
            "Gorjeta",
            "Outros",
        ).map { SuggestedCategory(name = it, type = TransactionType.INCOME) }

        fun defaultExpenseCategories() = listOf(
            "Carvão",
            "Transporte",
            "Aluguel",
            "Manutenção",
            "Outros",
        ).map { SuggestedCategory(name = it, type = TransactionType.EXPENSE) }

        fun defaultPaymentMethods() = listOf(
            "Dinheiro",
            "Pix",
            "Cartão de Débito",
            "Cartão de Crédito"
        ).map { SuggestedPaymentMethod(name = it) }
    }
}
