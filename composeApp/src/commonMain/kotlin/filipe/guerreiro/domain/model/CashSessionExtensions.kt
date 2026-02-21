package filipe.guerreiro.domain.model

import filipe.guerreiro.ui.cash.listing.CashSessionUi
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

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
