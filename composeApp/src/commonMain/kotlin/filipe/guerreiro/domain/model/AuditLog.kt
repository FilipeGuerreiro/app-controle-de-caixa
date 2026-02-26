package filipe.guerreiro.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlin.time.Instant

@Entity(
    tableName = "audit_logs",
    foreignKeys = [
        ForeignKey(
            entity = CashSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Transaction::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["transactionId"])
    ]
)
data class AuditLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val transactionId: Long,
    val actionType: AuditActionType,
    val oldValue: String,
    val newValue: String,
    val reason: String,
    val timestamp: Instant
)

enum class AuditActionType {
    UPDATE,
    DELETE
}
