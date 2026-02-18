package filipe.guerreiro.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import filipe.guerreiro.ui.cash.listing.CashSessionUi
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
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

fun CashSession.toUiModel(): CashSessionUi {
    val date = openingTimeStamp.toLocalDateTime(TimeZone.currentSystemDefault())
    val formattedDate = "${date.day}/${date.month.number}/${date.year}"

    return CashSessionUi(
        id = id,
        date = formattedDate,
        status = if (status == CashStatusType.OPEN) "Aberto" else "Fechado",
        finalBalance = initialAmount.toCurrencyString(),
        initialAmount = initialAmount.toCurrencyString(),
        isCurrent = status == CashStatusType.OPEN,
        timestamp = openingTimeStamp.toEpochMilliseconds()
    )
}
