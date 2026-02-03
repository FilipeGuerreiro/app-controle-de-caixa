package filipe.guerreiro.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Instant

@Entity(tableName = "cash_sessions")
data class CashSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val openingTimeStamp: Instant,
    val closingTimeStamp: Instant? = null,
    val initialAmount: Double,
    val status: String = "OPEN"
)