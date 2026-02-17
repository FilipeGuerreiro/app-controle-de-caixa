package filipe.guerreiro.data.local

import filipe.guerreiro.data.local.dao.CategoryDao
import filipe.guerreiro.domain.model.Category
import filipe.guerreiro.domain.model.TransactionType
import filipe.guerreiro.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class CategoryRepositoryImpl(
    private val dao: CategoryDao
) : CategoryRepository {
    override fun getCategories(userId: Long): Flow<List<Category>> {
        return dao.getAll(userId)
    }

    override suspend fun createCategory(userId: Long, name: String, type: TransactionType) {
        withContext(Dispatchers.IO) {
            val foundCategory = dao.getByName(userId, name)
            if (foundCategory != null) {
                throw IllegalStateException("Já existe uma categoria com esse nome.")
            }
            val newCategory = Category(
                userId = userId,
                name = name,
                type = type
            )
            dao.insert(newCategory)
        }
    }

    override suspend fun updateCategory(userId: Long, categoryId: Long, name: String, type: TransactionType) {
        withContext(Dispatchers.IO) {
            val foundCategory = dao.getByName(userId, name)
            if (foundCategory != null && foundCategory.id != categoryId) {
                throw IllegalStateException("Já existe uma categoria com esse nome.")
            }
            dao.update(categoryId, name, type)
        }
    }

    override suspend fun deleteCategory(userId: Long, categoryId: Long) {
        withContext(Dispatchers.IO) {
            dao.deleteById(categoryId)
        }
    }
}
