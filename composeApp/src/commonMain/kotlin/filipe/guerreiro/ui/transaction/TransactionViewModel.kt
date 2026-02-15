package filipe.guerreiro.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.domain.model.Category
import filipe.guerreiro.domain.model.PaymentMethod
import filipe.guerreiro.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TransactionUiState(
    val amount: String = "",
    val description: String = "",
    val type: TransactionType = TransactionType.INCOME,
    val selectedCategory: Category? = null,
    val selectedPaymentMethod: PaymentMethod? = null,
    val categories: List<Category> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val isSaved: Boolean = false
)

class TransactionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TransactionUiState())
    val uiState: StateFlow<TransactionUiState> = _uiState.asStateFlow()

    init {
        // Mock Data
        val mockCategories = listOf(
            Category(id = 1, userId = 1, name = "Vendas", type = TransactionType.INCOME),
            Category(id = 2, userId = 1, name = "Serviços", type = TransactionType.INCOME),
            Category(id = 3, userId = 1, name = "Aluguel", type = TransactionType.EXPENSE),
            Category(id = 4, userId = 1, name = "Fornecedores", type = TransactionType.EXPENSE),
            Category(id = 5, userId = 1, name = "Salários", type = TransactionType.EXPENSE),
            Category(id = 6, userId = 1, name = "Transporte", type = TransactionType.EXPENSE)
        )

        val mockPaymentMethods = listOf(
            PaymentMethod(id = 1, userId = 1, name = "Dinheiro"),
            PaymentMethod(id = 2, userId = 1, name = "Cartão de Crédito"),
            PaymentMethod(id = 3, userId = 1, name = "Pix"),
            PaymentMethod(id = 4, userId = 1, name = "Cartão de Débito")
        )

        _uiState.update {
            it.copy(
                categories = mockCategories,
                paymentMethods = mockPaymentMethods
            )
        }
    }

    fun onAmountChange(newAmount: String) {
        // Simple filter to allow only numbers and one dot/comma
        // For a real app, use a proper currency formatter
        if (newAmount.all { it.isDigit() || it == '.' || it == ',' }) {
             _uiState.update { it.copy(amount = newAmount) }
        }
    }

    fun onDescriptionChange(newDescription: String) {
        _uiState.update { it.copy(description = newDescription) }
    }

    fun onTypeChange(newType: TransactionType) {
        _uiState.update {
            it.copy(
                type = newType,
                selectedCategory = null // Reset category when type changes
            )
        }
    }

    fun onCategorySelected(category: Category) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onPaymentMethodSelected(paymentMethod: PaymentMethod) {
        _uiState.update { it.copy(selectedPaymentMethod = paymentMethod) }
    }

    fun saveTransaction() {
        // Simulate saving
        viewModelScope.launch {
            _uiState.update { it.copy(isSaved = true) }
        }
    }
    
    fun resetSaveState() {
        _uiState.update { it.copy(isSaved = false) }
    }
}
