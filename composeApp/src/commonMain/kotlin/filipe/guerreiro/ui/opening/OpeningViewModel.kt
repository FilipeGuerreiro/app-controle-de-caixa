package filipe.guerreiro.ui.opening

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.data.local.dao.CashDao
import filipe.guerreiro.domain.model.CashSession
import kotlinx.coroutines.launch
import kotlin.time.Clock

class OpeningViewModel(
    private val cashDao: CashDao
) : ViewModel() {
    var initialAmount by mutableStateOf("")
        private set

    fun onAmountChange(newValue: String) {
        initialAmount = newValue
    }

    fun openCash() {
        viewModelScope.launch {
            val amount = initialAmount.toDoubleOrNull() ?: 0.0
            val newSession = CashSession(
                openingTimeStamp = Clock.System.now(),
                initialAmount = amount,
                status = "OPEN"
            )
            cashDao.insertSession(newSession)

            println("Sessão inserida com sucesso!")

            cashDao.getAllSessions().collect { sessions ->
                println("Total de sessões no banco: ${sessions.size}")
                sessions.forEach {
                    println("ID: ${it.id}, Valor: ${it.initialAmount}, Status: ${it.status}")
                }
            }
        }
    }
}