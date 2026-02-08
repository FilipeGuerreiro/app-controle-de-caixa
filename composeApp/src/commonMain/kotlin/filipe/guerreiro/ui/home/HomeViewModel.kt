package filipe.guerreiro.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.data.SessionPreferences
import filipe.guerreiro.data.UserPreferences
import filipe.guerreiro.data.local.dao.CashDao
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userPreferences: UserPreferences,
    private val sessionPreferences: SessionPreferences
) : ViewModel() {
    fun disableUserRegistered() {
        viewModelScope.launch {
            userPreferences.setUserRegistered(false)
            sessionPreferences.clearSession()
            println("HomeViewModel: Session cleaned successfully.")

        }
    }
}