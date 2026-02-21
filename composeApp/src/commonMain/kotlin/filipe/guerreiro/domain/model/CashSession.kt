package filipe.guerreiro.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.time.Instant

@Entity(
    tableName = "cash_sessions",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["userId", "openingTimeStamp"])]
)
data class CashSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val openingTimeStamp: Instant,
    val closingTimeStamp: Instant? = null,
    val initialAmount: Long,
    val status: CashStatusType = CashStatusType.OPEN
)

enum class CashStatusType { OPEN, CLOSED }

