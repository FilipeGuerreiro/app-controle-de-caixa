package filipe.guerreiro.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.domain.model.Category
import filipe.guerreiro.domain.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class CategoryViewModel : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Mock data
        _categories.value = listOf(
            Category(id = 1, userId = 1, name = "Vendas", type = TransactionType.INCOME),
            Category(id = 2, userId = 1, name = "Serviços", type = TransactionType.INCOME),
            Category(id = 3, userId = 1, name = "Aluguel", type = TransactionType.EXPENSE),
            Category(id = 4, userId = 1, name = "Fornecedores", type = TransactionType.EXPENSE),
            Category(id = 5, userId = 1, name = "Salários", type = TransactionType.EXPENSE),
            Category(id = 6, userId = 1, name = "Transporte", type = TransactionType.EXPENSE)
        )
    }

    fun addCategory(name: String, type: TransactionType) {
        val newCategory = Category(
            id = (_categories.value.maxOfOrNull { it.id } ?: 0) + 1,
            userId = 1, // Mock user ID
            name = name,
            type = type
        )
        _categories.update { it + newCategory }
    }

    fun deleteCategory(id: Long) {
        _categories.update { list -> list.filter { it.id != id } }
    }
}
