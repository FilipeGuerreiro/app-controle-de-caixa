package filipe.guerreiro.ui.paymentmethod

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.domain.model.PaymentMethod
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PaymentMethodViewModel : ViewModel() {

    private val _paymentMethods = MutableStateFlow<List<PaymentMethod>>(emptyList())
    val paymentMethods: StateFlow<List<PaymentMethod>> = _paymentMethods
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Mock data
        _paymentMethods.value = listOf(
            PaymentMethod(id = 1, userId = 1, name = "Dinheiro"),
            PaymentMethod(id = 2, userId = 1, name = "Cartão de Crédito"),
            PaymentMethod(id = 3, userId = 1, name = "Pix"),
            PaymentMethod(id = 4, userId = 1, name = "Cartão de Débito")
        )
    }

    fun addPaymentMethod(name: String) {
        val newMethod = PaymentMethod(
            id = (_paymentMethods.value.maxOfOrNull { it.id } ?: 0) + 1,
            userId = 1, // Mock user ID
            name = name
        )
        _paymentMethods.update { it + newMethod }
    }

    fun deletePaymentMethod(id: Long) {
        _paymentMethods.update { list -> list.filter { it.id != id } }
    }
}
