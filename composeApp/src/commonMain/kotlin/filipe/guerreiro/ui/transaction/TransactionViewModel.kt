package filipe.guerreiro.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.domain.model.Category
import filipe.guerreiro.domain.model.PaymentMethod
import filipe.guerreiro.domain.model.TransactionType
import filipe.guerreiro.domain.repository.CategoryRepository
import filipe.guerreiro.domain.repository.PaymentMethodRepository
import filipe.guerreiro.domain.repository.TransactionRepository
import filipe.guerreiro.domain.session.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TransactionUiState(
    val amountInCents: Long = 0L,
    val description: String = "",
    val type: TransactionType = TransactionType.INCOME,
    val selectedCategory: Category? = null,
    val selectedPaymentMethod: PaymentMethod? = null,
    val categories: List<Category> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val isSaved: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)


data class TransactionDbState(
    val categories: List<Category> = emptyList(),
    val paymentMethods: List<PaymentMethod> = emptyList(),

)

class TransactionViewModel(
    private val sessionManager: SessionManager,
    private val categoryRepository: CategoryRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {


    @OptIn(ExperimentalCoroutinesApi::class)
    private val _dataBaseFlow = sessionManager.currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(TransactionDbState())
            else combine(
                categoryRepository.getCategories(user.id),
                paymentMethodRepository.getPaymentMethods(user.id)
            ) {
                categories, payments ->
                TransactionDbState(categories = categories, paymentMethods = payments)
            }
        }

    private val _localUiState = MutableStateFlow(TransactionUiState())

    val uiState: StateFlow<TransactionUiState> =
        combine(
            _dataBaseFlow,
            _localUiState
        ) { db, ui ->
            ui.copy(
                categories = db.categories,
                paymentMethods = db.paymentMethods
            )
        }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TransactionUiState()
    )


    fun onAmountChange(newAmount: Long) {
        _localUiState.update { it.copy(amountInCents = newAmount) }
    }

    fun onDescriptionChange(newDescription: String) {
        _localUiState.update { it.copy(description = newDescription) }
    }

    fun onTypeChange(newType: TransactionType) {
        _localUiState.update {
            it.copy(
                type = newType,
                selectedCategory = null // Reset category when type changes
            )
        }
    }

    fun onCategorySelected(category: Category) {
        _localUiState.update { it.copy(selectedCategory = category) }
    }

    fun onPaymentMethodSelected(paymentMethod: PaymentMethod) {
        _localUiState.update { it.copy(selectedPaymentMethod = paymentMethod) }
    }

    fun saveTransaction() {
        val currentState = _localUiState.value
        val category = currentState.selectedCategory
        val paymentMethod = currentState.selectedPaymentMethod

        val validationError = when {
            currentState.amountInCents <= 0L -> "Informe um valor maior que zero"
            category == null -> "Selecione uma categoria"
            paymentMethod == null -> "Selecione um método de pagamento"
            else -> null
        }

        if (validationError != null) {
            _localUiState.update { it.copy(errorMessage = validationError) }
            return
        }

        viewModelScope.launch {
            _localUiState.update { it.copy(isSaving = true, errorMessage = null) }
            try {
                val user = sessionManager.currentUser.value
                if (user == null) {
                    _localUiState.update {
                        it.copy(
                            isSaving = false,
                            errorMessage = "Nenhum usuário logado."
                        )
                    }
                    return@launch
                }

                transactionRepository.addTransactionForUserCurrentSession(
                    userId = user.id,
                    categoryId = category!!.id,
                    paymentMethodId = paymentMethod!!.id,
                    amount = currentState.amountInCents,
                    description = currentState.description,
                    type = currentState.type
                )

                _localUiState.update {
                    it.copy(
                        isSaving = false,
                        isSaved = true
                    )
                }
            } catch (e: Exception) {
                _localUiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = e.message ?: "Erro ao salvar lançamento"
                    )
                }
            }
        }
    }
    
    fun resetSaveState() {
        _localUiState.update { it.copy(isSaved = false) }
    }
}
