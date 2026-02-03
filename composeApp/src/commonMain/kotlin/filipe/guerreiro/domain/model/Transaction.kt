package filipe.guerreiro.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Instant

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long, //fk da CashSession
    val amount: Double,
    val description: String,
    val type: TransactionType,
    val timestamp: Instant
)

enum class TransactionType { INCOME, EXPENSE }