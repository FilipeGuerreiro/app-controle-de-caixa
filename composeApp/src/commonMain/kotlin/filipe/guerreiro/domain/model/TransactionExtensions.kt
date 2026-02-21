package filipe.guerreiro.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import filipe.guerreiro.ui.cash.detail.HistoryItemUi
import filipe.guerreiro.ui.home.RecentActivity
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun Transaction.toRecentActivity(): RecentActivity {
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

fun Transaction.toHistoryUi(): HistoryItemUi {
    val isIncome = type.name == TransactionType.INCOME.name

    return HistoryItemUi(
        id = sessionId.toString(),
        title = description,
        time = timestamp.toLocalDateTime(TimeZone.currentSystemDefault()).toString(),
        method = type.name,
        amountLabel = amount.toCurrencyString(),
        isIncome = isIncome,
        icon = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
    )
}
