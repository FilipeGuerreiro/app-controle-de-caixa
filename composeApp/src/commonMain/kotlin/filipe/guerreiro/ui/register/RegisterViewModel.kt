package filipe.guerreiro.ui.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.data.UserPreferences
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val userPreferences: UserPreferences
) : ViewModel() {
    fun onFinishRegister() {
        viewModelScope.launch {
            userPreferences.setUserRegistered(true)
        }
    }
}