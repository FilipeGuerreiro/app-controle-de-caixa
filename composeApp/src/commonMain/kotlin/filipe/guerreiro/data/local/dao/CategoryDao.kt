package filipe.guerreiro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import filipe.guerreiro.domain.model.Category
import filipe.guerreiro.domain.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories WHERE userId = :userId")
    fun getAll(userId: Long): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE name = :name AND userId = :userId LIMIT 1")
    suspend fun getByName(userId: Long, name: String): Category?

    @Insert
    suspend fun insert(category: Category)

    @Query("UPDATE categories SET name = :name, type = :type WHERE id = :id")
    suspend fun update(id: Long, name: String, type: TransactionType)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: Long)
}
