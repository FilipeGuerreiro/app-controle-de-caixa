package filipe.guerreiro.domain.repository

import filipe.guerreiro.domain.model.Category
import filipe.guerreiro.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun getCategories(userId: Long): Flow<List<Category>>
    suspend fun createCategory(userId: Long, name: String, type: TransactionType)
    suspend fun updateCategory(userId: Long, categoryId: Long, name: String, type: TransactionType)
    suspend fun deleteCategory(userId: Long, categoryId: Long)
}
