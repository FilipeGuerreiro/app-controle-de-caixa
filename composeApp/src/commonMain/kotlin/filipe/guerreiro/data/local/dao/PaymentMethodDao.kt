package filipe.guerreiro.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import filipe.guerreiro.domain.model.PaymentMethod
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentMethodDao {
    @Query("SELECT * FROM payment_methods WHERE userId = :userId AND isActive = 1")
    fun getAll(userId: Long): Flow<List<PaymentMethod>>

    @Query("SELECT * FROM payment_methods WHERE name = :name AND userId = :userId AND isActive = 1 LIMIT 1")
    suspend fun getByName(userId: Long, name: String): PaymentMethod?

    @Insert
    suspend fun insert(paymentMethod: PaymentMethod): Long

    @Query("UPDATE payment_methods SET name = :name WHERE id = :id")
    suspend fun update(id: Long, name: String)

    @Query("UPDATE payment_methods SET isActive = 0 WHERE id = :id")
    suspend fun deleteById(id: Long)
}
