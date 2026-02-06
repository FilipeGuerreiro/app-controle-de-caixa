package filipe.guerreiro.core.startup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppViewModel(
    private val resolver: AppStartupResolver
) : ViewModel() {

    private val _startState = MutableStateFlow<AppStartState>(AppStartState.Loading)
    val startState: StateFlow<AppStartState> = _startState
    init {
        initialize()
    }

    private fun initialize() {
        viewModelScope.launch {
            _startState.value = resolver.resolveStartState()
        }
    }
}