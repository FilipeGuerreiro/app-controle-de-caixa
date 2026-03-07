package filipe.guerreiro.domain.usecase

import filipe.guerreiro.domain.model.CashSession
import filipe.guerreiro.domain.model.Category
import filipe.guerreiro.domain.model.PaymentMethod
import filipe.guerreiro.domain.model.Transaction
import filipe.guerreiro.domain.model.TransactionType
import filipe.guerreiro.domain.model.toCurrencyString
import filipe.guerreiro.domain.service.XlsxCell
import filipe.guerreiro.domain.service.XlsxStyle
import filipe.guerreiro.domain.service.XlsxWriter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number

data class DailyAggregate(
    val date: LocalDate,
    val totalInflow: Long,
    val totalOutflow: Long,
    val sessions: List<CashSession>
)

class GenerateWeeklyReportUseCase {

    operator fun invoke(
        periodLabel: String,
        dailyAggregates: List<DailyAggregate>,
        categories: List<Category>,
        paymentMethods: List<PaymentMethod>,
        allTransactions: Map<Long, List<Transaction>>  // sessionId -> transactions
    ): ByteArray {
        val weeklyInflow = dailyAggregates.sumOf { it.totalInflow }
        val weeklyOutflow = dailyAggregates.sumOf { it.totalOutflow }

        val firstSession = dailyAggregates.firstOrNull()?.sessions?.firstOrNull()
        val weekOpening = firstSession?.initialAmount ?: 0L
        val weekNet = weeklyInflow - weeklyOutflow

        // Day-of-week in Portuguese
        fun dayOfWeekPt(date: LocalDate): String = when (date.dayOfWeek.name) {
            "MONDAY"    -> "Segunda"
            "TUESDAY"   -> "Terça"
            "WEDNESDAY" -> "Quarta"
            "THURSDAY"  -> "Quinta"
            "FRIDAY"    -> "Sexta"
            "SATURDAY"  -> "Sábado"
            "SUNDAY"    -> "Domingo"
            else        -> ""
        }

        val rows = mutableListOf<List<XlsxCell>>()

        // --- Header Section ---
        rows.add(listOf(XlsxCell("FLUXO DE CAIXA - $periodLabel", XlsxStyle.TITLE)))
        rows.add(listOf(
            XlsxCell("Data", XlsxStyle.HEADER),
            XlsxCell("Vendas", XlsxStyle.HEADER),
            XlsxCell("Valor", XlsxStyle.HEADER),
            XlsxCell("Observação", XlsxStyle.HEADER),
            XlsxCell("Retiradas", XlsxStyle.HEADER),
            XlsxCell("Observação", XlsxStyle.HEADER)
        ))

        // --- Daily Breakdown Section ---
        for (agg in dailyAggregates) {
            val dateStr = "${agg.date.day.toString().padStart(2, '0')}/${agg.date.month.number.toString().padStart(2, '0')}/${agg.date.year}"
            val noWork = agg.sessions.isEmpty() && agg.totalInflow == 0L && agg.totalOutflow == 0L
            
            if (noWork) {
                // Skip empty days or show a single row
                continue // Skipping empty days makes the report much cleaner, just like manual spreadsheets
            }

            val dayTxs = agg.sessions.flatMap { s -> allTransactions[s.id] ?: emptyList() }
            
            // 1. Group Incomes by Payment Method for this day
            val incomesByMethod = dayTxs
                .filter { it.type == TransactionType.INCOME }
                .groupBy { it.paymentMethodId }
                .map { (pmId, txs) ->
                    val pmName = paymentMethods.find { it.id == pmId }?.name ?: "Sem Método"
                    val total = txs.sumOf { it.amount }
                    val obs = txs.mapNotNull { it.description.ifEmpty { null } }
                        .distinct()
                        .joinToString("; ")
                    Triple(pmName, total, obs)
                }
                .sortedByDescending { it.second }

            // 2. Individual Expenses for this day
            val expenses = dayTxs
                .filter { it.type == TransactionType.EXPENSE }
                .sortedBy { it.timestamp }
                .map { tx ->
                    val obs = tx.description.ifEmpty { "—" }
                    Pair(tx.amount, obs)
                }

            val maxRows = maxOf(incomesByMethod.size, expenses.size, 1)
            
            for (i in 0 until maxRows) {
                val isEven = rows.size % 2 == 0
                val baseStyle = if (isEven) XlsxStyle.NORMAL else XlsxStyle.STRIPE
                val emptyStyle = if (isEven) XlsxStyle.NORMAL else XlsxStyle.STRIPE
                
                // Print date only on the first row of this day
                val dateCellStr = if (i == 0) dateStr else ""

                // Income cell logic
                val inc = incomesByMethod.getOrNull(i)
                val incMethod = inc?.first ?: ""
                val incTotalStr = inc?.second?.toCurrencyString() ?: ""
                val incObs = inc?.third ?: ""
                val incStyleVal = if (inc != null) {
                    if (isEven) XlsxStyle.PROFIT else XlsxStyle.STRIPE_PROFIT
                } else emptyStyle

                // Expense cell logic
                val exp = expenses.getOrNull(i)
                val expTotalStr = exp?.first?.toCurrencyString() ?: ""
                val expObs = exp?.second ?: ""
                val expStyleVal = if (exp != null) {
                    if (isEven) XlsxStyle.LOSS else XlsxStyle.STRIPE_LOSS
                } else emptyStyle

                rows.add(listOf(
                    XlsxCell(dateCellStr, if (dateCellStr.isNotEmpty()) XlsxStyle.TITLE else baseStyle), // Highlight date slightly if present
                    XlsxCell(incMethod, baseStyle),
                    XlsxCell(incTotalStr, incStyleVal),
                    XlsxCell(incObs, baseStyle),
                    XlsxCell(expTotalStr, expStyleVal),
                    XlsxCell(expObs, baseStyle)
                ))
            }
        }

        // --- Bottom Summary Section ---
        val totalInBox = weekOpening + weeklyInflow
        val netBalanceStyle = if (weekNet >= 0) XlsxStyle.PROFIT else XlsxStyle.LOSS

        rows.add(emptyList()) // spacing
        rows.add(listOf(
            XlsxCell("Vendas da semana", XlsxStyle.TITLE),
            XlsxCell("", XlsxStyle.NORMAL),
            XlsxCell(weeklyInflow.toCurrencyString(), XlsxStyle.PROFIT),
            XlsxCell("", XlsxStyle.NORMAL),
            XlsxCell("Total de Retirada", XlsxStyle.TITLE),
            XlsxCell(weeklyOutflow.toCurrencyString(), XlsxStyle.LOSS)
        ))
        
        rows.add(emptyList()) // spacing
        rows.add(listOf(
            XlsxCell("Abertura da semana", XlsxStyle.PROFIT),
            XlsxCell("", XlsxStyle.NORMAL),
            XlsxCell(weekOpening.toCurrencyString(), XlsxStyle.PROFIT)
        ))
        
        rows.add(listOf(
            XlsxCell("Total em caixa", XlsxStyle.TITLE),
            XlsxCell("", XlsxStyle.NORMAL),
            XlsxCell(totalInBox.toCurrencyString(), XlsxStyle.NORMAL),
            XlsxCell("", XlsxStyle.NORMAL),
            XlsxCell("", XlsxStyle.NORMAL),
            XlsxCell(weeklyOutflow.toCurrencyString(), XlsxStyle.NORMAL)
        ))
        
        rows.add(listOf(
            XlsxCell("Total Retirada", XlsxStyle.LOSS),
            XlsxCell("", XlsxStyle.NORMAL),
            XlsxCell(weeklyOutflow.toCurrencyString(), XlsxStyle.NORMAL)
        ))
        
        rows.add(listOf(
            XlsxCell("Fechamento da semana", netBalanceStyle),
            XlsxCell("", XlsxStyle.NORMAL),
            XlsxCell(weekNet.toCurrencyString(), netBalanceStyle)
        ))

        // Generate XLSX
        val xlsxWriter = XlsxWriter(sheetName = "Semanal $periodLabel", rows = rows)
        return xlsxWriter.generateXlsxRawBytes()
    }
}
