package filipe.guerreiro.domain.model

import filipe.guerreiro.ui.cash.detail.CashSummaryUi
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.number
import kotlin.time.Instant

data class SessionBalance(
    val initial: Long,
    val totalIncomes: Long,
    val totalExpenses: Long,
    val currentBalance: Long
)

fun SessionBalance.toSummaryUi(
    statusType: CashStatusType,
    openingTime: Instant,
    closingTime: Instant?
): CashSummaryUi {
    val delta = currentBalance - initial
    val isPositive = delta >= 0

    val open = openingTime.toLocalDateTime(TimeZone.currentSystemDefault())
    val openingStr = "${open.day}/${open.month.number}/${open.year} ${open.hour.toString().padStart(2, '0')}:${open.minute.toString().padStart(2, '0')}"

    val closingStr = closingTime?.let {
        val close = it.toLocalDateTime(TimeZone.currentSystemDefault())
        "${close.day}/${close.month.number}/${close.year} ${close.hour.toString().padStart(2, '0')}:${close.minute.toString().padStart(2, '0')}"
    }

    return CashSummaryUi(
        initialAmount = initial.toCurrencyString(),
        currentBalance = currentBalance.toCurrencyString(),
        totalInflow = totalIncomes.toCurrencyString(),
        totalOutflow = totalExpenses.toCurrencyString(),
        status = if (statusType.name == "OPEN") "Aberto" else "Fechado",
        openingDate = openingStr,
        closingDate = closingStr,
        balanceDelta = (delta).toCurrencyString(),
        isDeltaPositive = isPositive,
        initialAmountValue = initial,
        currentBalanceValue = currentBalance,
        totalInflowValue = totalIncomes,
        totalOutflowValue = totalExpenses
    )
}
