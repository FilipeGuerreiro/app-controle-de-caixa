package filipe.guerreiro.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import filipe.guerreiro.ui.cash.detail.HistoryItemUi
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
