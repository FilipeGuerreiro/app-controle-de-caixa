package filipe.guerreiro.domain.model

import filipe.guerreiro.ui.cash.detail.CashSummaryUi

data class SessionBalance(
    val initial: Long,
    val totalIncomes: Long,
    val totalExpenses: Long,
    val currentBalance: Long
)

fun SessionBalance.toSummaryUi(statusType: CashStatusType): CashSummaryUi {
    return CashSummaryUi(
        initialAmount = initial.toCurrencyString(),
        currentBalance = currentBalance.toCurrencyString(),
        totalInflow = totalIncomes.toCurrencyString(),
        totalOutflow = totalExpenses.toCurrencyString(),
        status = if (statusType.name == "OPEN") "Aberto" else "Fechado"
    )
}
