package filipe.guerreiro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import filipe.guerreiro.domain.model.User

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    suspend fun getAll(): List<User>

    @Query("SELECT COUNT(*) FROM users")
    suspend fun countUsers(): Int

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: Long): User?

    @Insert
    suspend fun insert(user: User): Long
}