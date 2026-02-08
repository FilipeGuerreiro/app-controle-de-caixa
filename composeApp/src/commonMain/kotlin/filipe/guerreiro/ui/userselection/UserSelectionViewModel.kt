package filipe.guerreiro.ui.userselection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.data.SessionPreferences
import filipe.guerreiro.data.UserPreferences
import filipe.guerreiro.domain.model.User
import filipe.guerreiro.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UserSelectionUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class UserSelectionViewModel(
    private val userRepository: UserRepository,
    private val sessionPreferences: SessionPreferences,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserSelectionUiState())
    val uiState: StateFlow<UserSelectionUiState> = _uiState

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val users = userRepository.getAllUsers()
                _uiState.value = _uiState.value.copy(
                    users = users,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Não foi possível carregar os usuários."
                )
            }
        }
    }

    fun selectUser(userId: Long, onSuccess: () -> Unit) {
        if (_uiState.value.isLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                sessionPreferences.setLoggedUserId(userId)
                userPreferences.setUserRegistered(true)
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Não foi possível selecionar o usuário."
                )
            }
        }
    }

}