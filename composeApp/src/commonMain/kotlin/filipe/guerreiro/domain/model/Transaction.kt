package filipe.guerreiro.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import filipe.guerreiro.ui.home.RecentActivity
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CashSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["sessionId"])]
)
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val categoryId: Long,
    val paymentMethodId: Long,
    val amount: Long,
    val description: String,
    val type: TransactionType,
    val timestamp: Instant
)

enum class TransactionType { INCOME, EXPENSE }

;fun Transaction.toRecentActivity(): RecentActivity {
    val isIncome = type == TransactionType.INCOME
    val localDateTime =
        timestamp.toLocalDateTime(TimeZone.currentSystemDefault())

    val timeStr =
        "${localDateTime.hour.toString().padStart(2, '0')}:${
            localDateTime.minute.toString().padStart(2, '0')
        }"

    return RecentActivity(
        id = id.toString(),
        title = description.ifEmpty {
            if (isIncome) "Entrada" else "Saída"
        },
        time = timeStr,
        type = if (isIncome) "Entrada" else "Saída",
        amount = amount,
        isIncome = isIncome,
        icon = if (isIncome)
            Icons.Default.ArrowUpward
        else
            Icons.Default.ArrowDownward
    )
}