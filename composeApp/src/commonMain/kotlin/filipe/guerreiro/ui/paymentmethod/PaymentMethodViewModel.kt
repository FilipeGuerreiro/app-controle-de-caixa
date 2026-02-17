package filipe.guerreiro.ui.paymentmethod

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.domain.model.PaymentMethod
import filipe.guerreiro.domain.repository.PaymentMethodRepository
import filipe.guerreiro.domain.session.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PaymentMethodUiState(
    val paymentMethods: List<PaymentMethod> = emptyList(),
    val isLoading: Boolean = true,
    val isLogged: Boolean = true,
    val error: String? = null,
    val showAddDialog: Boolean = false,
    val dialogNameInput: String = "",
    val dialogError: String? = null,
    val showEditDialog: Boolean = false,
    val editDialogNameInput: String = "",
    val editDialogError: String? = null,
    val showDeleteDialog: Boolean = false,
    val deleteDialogName: String = ""
)

class PaymentMethodViewModel(
    private val repository: PaymentMethodRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    
    // Estados do Diálogo de Adição
    private val _showAddDialog = MutableStateFlow(false)
    private val _dialogNameInput = MutableStateFlow("")
    private val _dialogError = MutableStateFlow<String?>(null)

    // Estados do Diálogo de Edição
    private val _showEditDialog = MutableStateFlow(false)
    private val _editDialogNameInput = MutableStateFlow("")
    private val _editDialogError = MutableStateFlow<String?>(null)
    private val _editingPaymentMethodId = MutableStateFlow<Long?>(null)

    // Estado do Diálogo de Exclusão
    private val _showDeleteDialog = MutableStateFlow(false)
    private val _deleteDialogName = MutableStateFlow("")
    private val _deletingPaymentMethodId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _paymentMethodsFlow = sessionManager.currentUser
            .flatMapLatest { user ->
                if (user == null) flowOf(emptyList())
                else repository.getPaymentMethods(user.id)
            }

    private val _baseUiState = combine(
        _paymentMethodsFlow,
        _isLoading,
        _errorMessage,
        _showAddDialog,
        _dialogNameInput
    ) { methods, isLoading, error, showDialog, inputName ->
        PaymentMethodUiState(
            paymentMethods = methods,
            isLoading = isLoading,
            error = error,
            showAddDialog = showDialog,
            dialogNameInput = inputName
        )
    }

    private val _addDialogUiState = combine(
        _baseUiState,
        _dialogError
    ) { state, dialogError ->
        state.copy(dialogError = dialogError)
    }

    private val _editDialogUiState = combine(
        _addDialogUiState,
        _showEditDialog,
        _editDialogNameInput,
        _editDialogError
    ) { state, showEdit, editName, editError ->
        state.copy(
            showEditDialog = showEdit,
            editDialogNameInput = editName,
            editDialogError = editError
        )
    }

    val uiState: StateFlow<PaymentMethodUiState> = combine(
        _editDialogUiState,
        _showDeleteDialog,
        _deleteDialogName
    ) { state, showDelete, deleteName ->
        state.copy(
            showDeleteDialog = showDelete,
            deleteDialogName = deleteName
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PaymentMethodUiState(isLoading = true)
    )

    // Ações do Diálogo
    fun onShowAddDialog() {
        _dialogNameInput.value = ""
        _dialogError.value = null
        _showAddDialog.value = true
    }

    fun onDismissAddDialog() {
        _showAddDialog.value = false
        _dialogNameInput.value = ""
        _dialogError.value = null
    }

    fun onDialogNameChange(newName: String) {
        _dialogNameInput.value = newName
        if (_dialogError.value != null) {
            _dialogError.value = null
        }
    }

    fun onConfirmAddDialog() {
        val name = _dialogNameInput.value.trim()
        if (name.isBlank()) {
            _dialogError.value = "O nome não pode ser vazio"
            return
        }

        addPaymentMethod(name)
    }

     private fun addPaymentMethod(name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = sessionManager.currentUser.value
                if (user != null) {
                    repository.createPaymentMethod(user.id, name)
                    _errorMessage.value = null
                    onDismissAddDialog()
                }
            } catch (e: Exception) {
                _dialogError.value = e.message ?: "Erro desconhecido"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onShowEditDialog(method: PaymentMethod) {
        _editingPaymentMethodId.value = method.id
        _editDialogNameInput.value = method.name
        _editDialogError.value = null
        _showEditDialog.value = true
    }

    fun onDismissEditDialog() {
        _showEditDialog.value = false
        _editDialogNameInput.value = ""
        _editDialogError.value = null
        _editingPaymentMethodId.value = null
    }

    fun onEditDialogNameChange(newName: String) {
        _editDialogNameInput.value = newName
        if (_editDialogError.value != null) {
            _editDialogError.value = null
        }
    }

    fun onConfirmEditDialog() {
        val id = _editingPaymentMethodId.value ?: return
        val name = _editDialogNameInput.value.trim()
        if (name.isBlank()) {
            _editDialogError.value = "O nome não pode ser vazio"
            return
        }
        updatePaymentMethod(id, name)
    }

    private fun updatePaymentMethod(id: Long, name: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = sessionManager.currentUser.value
                if (user != null) {
                    repository.updatePaymentMethod(user.id, id, name)
                    _errorMessage.value = null
                    onDismissEditDialog()
                }
            } catch (e: Exception) {
                _editDialogError.value = e.message ?: "Erro ao atualizar"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun onShowDeleteDialog(method: PaymentMethod) {
        _deletingPaymentMethodId.value = method.id
        _deleteDialogName.value = method.name
        _showDeleteDialog.value = true
    }

    fun onDismissDeleteDialog() {
        _showDeleteDialog.value = false
        _deleteDialogName.value = ""
        _deletingPaymentMethodId.value = null
    }

    fun onConfirmDeleteDialog() {
        val id = _deletingPaymentMethodId.value ?: return
        deletePaymentMethod(id)
    }

    private fun deletePaymentMethod(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = sessionManager.currentUser.value
                if (user != null) {
                    repository.deletePaymentMethod(user.id, id)
                    _errorMessage.value = null
                    onDismissDeleteDialog()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao salvar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    
}
