package filipe.guerreiro.core.startup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
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
            // Executa duas tarefas simultaneamente (concorrentes)
            // 1. O delay mínimo para a animação da barra respirar (1500ms = 1.5s)
            val minDelayTask = async { delay(2500) }

            // 2. A resolução real do estado do usuário/banco de dados
            val resolveStateTask = async { resolver.resolveStartState() }

            // Primeiro, garantimos que o tempo mínimo de tela passou
            minDelayTask.await()

            // Depois, pegamos o resultado da resolução (se demorou mais que 1.5s, ele já terá o valor pronto)
            _startState.value = resolveStateTask.await()
        }
    }
}