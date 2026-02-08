package filipe.guerreiro.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.data.SessionPreferences
import filipe.guerreiro.data.UserPreferences
import filipe.guerreiro.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class RegisterUiState(
    val name: String = "",
    val businessName: String = "",
    val isLoading: Boolean = false,
    val showErrors: Boolean = false,
    val errorMessage: String? = null
)

class RegisterViewModel(
    private val userRepository: UserRepository,
    private val sessionPreferences: SessionPreferences,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState

    fun onNameChange(value: String) {
        _uiState.value = _uiState.value.copy(
            name = value,
            errorMessage = null
        )
    }

    fun onBusinessNameChange(value: String) {
        _uiState.value = _uiState.value.copy(
            businessName = value,
            errorMessage = null
        )
    }

    fun register(onSuccess: () -> Unit) {
        val current = _uiState.value
        val name = current.name.trim()
        val businessName = current.businessName.trim()

        if (current.isLoading) return

        if (name.isBlank() || businessName.isBlank()) {
            _uiState.value = current.copy(showErrors = true)
            return
        }

        viewModelScope.launch {
            _uiState.value = current.copy(isLoading = true, errorMessage = null)
            try {
                val user = userRepository.createUser(name, businessName)
                sessionPreferences.setLoggedUserId(user.id)
                userPreferences.setUserRegistered(true)
                _uiState.value = _uiState.value.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Não foi possível criar o usuário. Tente novamente."
                )
            }
        }
    }
}
