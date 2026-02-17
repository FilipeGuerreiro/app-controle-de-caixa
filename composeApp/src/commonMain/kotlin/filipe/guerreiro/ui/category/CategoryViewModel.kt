package filipe.guerreiro.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.domain.model.Category
import filipe.guerreiro.domain.model.TransactionType
import filipe.guerreiro.domain.repository.CategoryRepository
import filipe.guerreiro.domain.session.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CategoryUiState(
    val categories: List<Category> = emptyList(),
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

class CategoryViewModel(
    private val repository: CategoryRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)

    private val _showAddDialog = MutableStateFlow(false)
    private val _dialogNameInput = MutableStateFlow("")
    private val _dialogError = MutableStateFlow<String?>(null)

    private val _showEditDialog = MutableStateFlow(false)
    private val _editDialogNameInput = MutableStateFlow("")
    private val _editDialogError = MutableStateFlow<String?>(null)
    private val _editingCategoryId = MutableStateFlow<Long?>(null)

    private val _showDeleteDialog = MutableStateFlow(false)
    private val _deleteDialogName = MutableStateFlow("")
    private val _deletingCategoryId = MutableStateFlow<Long?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _categoriesFlow = sessionManager.currentUser
        .flatMapLatest { user ->
            if (user == null) flowOf(emptyList())
            else repository.getCategories(user.id)
        }

    val _baseUiState = combine(
        _categoriesFlow,
        _isLoading,
        _errorMessage,
        _showAddDialog,
        _dialogNameInput
    ) { categories, isLoading, error, showDialog, inputName ->
        CategoryUiState(
            categories = categories,
            isLoading = isLoading,
            error = error,
            showAddDialog = showDialog,
            dialogNameInput = inputName
        )
    }

    private val _addDialogUiState = combine(
        _baseUiState,
        _dialogError
    ) { state, error ->
        state.copy(dialogError = error)
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

    val uiState: StateFlow<CategoryUiState> = combine(
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
        initialValue = CategoryUiState(isLoading = true)
    )

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

    fun onConfirmAddDialog(type: TransactionType) {
        val name = _dialogNameInput.value.trim()
        if (name.isBlank()) {
            _dialogError.value = "O nome não pode ser vazio"
            return
        }

        addCategory(name, type)
    }

    private fun addCategory(name: String, type: TransactionType) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = sessionManager.currentUser.value
                if (user != null) {
                    repository.createCategory(user.id, name, type)
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

    fun onShowEditDialog(category: Category) {
        _editingCategoryId.value = category.id
        _editDialogNameInput.value = category.name
        _editDialogError.value = null
        _showEditDialog.value = true
    }

    fun onDismissEditDialog() {
        _showEditDialog.value = false
        _editDialogNameInput.value = ""
        _editDialogError.value = null
        _editingCategoryId.value = null
    }

    fun onEditDialogNameChange(newName: String) {
        _editDialogNameInput.value = newName
        if (_editDialogError.value != null) {
            _editDialogError.value = null
        }
    }

    fun onConfirmEditDialog(type: TransactionType) {
        val id = _editingCategoryId.value ?: return
        val name = _editDialogNameInput.value.trim()
        if (name.isBlank()) {
            _editDialogError.value = "O nome não pode ser vazio"
            return
        }
        updateCategory(id, name, type)
    }

    private fun updateCategory(id: Long, name: String, type: TransactionType) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = sessionManager.currentUser.value
                if (user != null) {
                    repository.updateCategory(user.id, id, name, type)
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

    fun onShowDeleteDialog(category: Category) {
        _deletingCategoryId.value = category.id
        _deleteDialogName.value = category.name
        _showDeleteDialog.value = true
    }

    fun onDismissDeleteDialog() {
        _showDeleteDialog.value = false
        _deleteDialogName.value = ""
        _deletingCategoryId.value = null
    }

    fun onConfirmDeleteDialog() {
        val id = _deletingCategoryId.value ?: return
        deleteCategory(id)
    }

    private fun deleteCategory(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val user = sessionManager.currentUser.value
                if (user != null) {
                    repository.deleteCategory(user.id, id)
                    _errorMessage.value = null
                    onDismissDeleteDialog()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao deletar: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
