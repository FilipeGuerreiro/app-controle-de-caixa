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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

class GenerateDailyReportUseCase {

    operator fun invoke(
        session: CashSession,
        transactions: List<Transaction>,
        categories: List<Category>,
        paymentMethods: List<PaymentMethod>,
        totalInflow: Long,
        totalOutflow: Long,
        currentBalance: Long
    ): ByteArray {
        val tz = TimeZone.currentSystemDefault()
        val opening = session.openingTimeStamp.toLocalDateTime(tz)
        val dateStr = "${opening.day.toString().padStart(2, '0')}/${opening.month.number.toString().padStart(2, '0')}/${opening.year}"
        val openingTimeStr = "${opening.hour.toString().padStart(2, '0')}:${opening.minute.toString().padStart(2, '0')}"

        val closingStr = session.closingTimeStamp?.toLocalDateTime(tz)?.let { c ->
            "${c.hour.toString().padStart(2, '0')}:${c.minute.toString().padStart(2, '0')}"
        } ?: "Em aberto"

        val netBalance = session.initialAmount + totalInflow - totalOutflow

        val rows = mutableListOf<List<XlsxCell>>()

        // --- Header Section ---
        rows.add(listOf(XlsxCell("FLUXO DE CAIXA DIÁRIO - $dateStr", XlsxStyle.TITLE)))
        rows.add(listOf(
            XlsxCell("Data", XlsxStyle.HEADER),
            XlsxCell("Vendas", XlsxStyle.HEADER),
            XlsxCell("Valor", XlsxStyle.HEADER),
            XlsxCell("Observação", XlsxStyle.HEADER),
            XlsxCell("Retiradas", XlsxStyle.HEADER),
            XlsxCell("Observação", XlsxStyle.HEADER)
        ))

        // --- Data Extraction ---
        // 1. Group Incomes by Payment Method
        val incomesByMethod = transactions
            .filter { it.type == TransactionType.INCOME }
            .groupBy { it.paymentMethodId }
            .map { (pmId, txs) ->
                val pmName = paymentMethods.find { it.id == pmId }?.name ?: "Sem Método"
                val total = txs.sumOf { it.amount }
                // Aggregate descriptions if any exist
                val obs = txs.mapNotNull { it.description.ifEmpty { null } }
                    .distinct()
                    .joinToString("; ")
                Triple(pmName, total, obs)
            }
            .sortedByDescending { it.second }

        // 2. Individual Expenses
        val expenses = transactions
            .filter { it.type == TransactionType.EXPENSE }
            .sortedBy { it.timestamp }
            .map { tx ->
                val obs = tx.description.ifEmpty { "—" }
                Pair(tx.amount, obs)
            }

        // --- Rows Assembly ---
        val maxRows = maxOf(incomesByMethod.size, expenses.size)
        
        if (maxRows == 0) {
            rows.add(listOf(XlsxCell("Nenhuma movimentação registrada no caixa", XlsxStyle.NORMAL)))
        } else {
            for (i in 0 until maxRows) {
                val isEven = i % 2 == 0
                val baseStyle = if (isEven) XlsxStyle.NORMAL else XlsxStyle.STRIPE
                val emptyStyle = if (isEven) XlsxStyle.NORMAL else XlsxStyle.STRIPE
                
                // Date only on the first row
                val dateCellStr = if (i == 0) dateStr else ""
                
                // Income part
                val inc = incomesByMethod.getOrNull(i)
                val incMethod = inc?.first ?: ""
                val incTotalStr = inc?.second?.toCurrencyString() ?: ""
                val incObs = inc?.third ?: ""
                val incStyleVal = if (inc != null) {
                    if (isEven) XlsxStyle.PROFIT else XlsxStyle.STRIPE_PROFIT
                } else emptyStyle

                // Expense part
                val exp = expenses.getOrNull(i)
                val expTotalStr = exp?.first?.toCurrencyString() ?: ""
                val expObs = exp?.second ?: ""
                val expStyleVal = if (exp != null) {
                    if (isEven) XlsxStyle.LOSS else XlsxStyle.STRIPE_LOSS
                } else emptyStyle

                rows.add(listOf(
                    XlsxCell(dateCellStr, baseStyle),
                    XlsxCell(incMethod, baseStyle),
                    XlsxCell(incTotalStr, incStyleVal),
                    XlsxCell(incObs, baseStyle),
                    XlsxCell(expTotalStr, expStyleVal),
                    XlsxCell(expObs, baseStyle)
                ))
            }
        }

        // --- Bottom Summary Section ---
        val totalInBox = session.initialAmount + totalInflow
        val netBalanceStyle = if (netBalance >= session.initialAmount) XlsxStyle.PROFIT else XlsxStyle.LOSS

        rows.add(emptyList()) // spacing
        rows.add(listOf(
            XlsxCell("Vendas do dia", XlsxStyle.TITLE),
            XlsxCell("", XlsxStyle.NORMAL),
            XlsxCell(totalInflow.toCurrencyString(), XlsxStyle.PROFIT),
            XlsxCell("", XlsxStyle.NORMAL),
            XlsxCell("Total de Retirada", XlsxStyle.TITLE),
            XlsxCell(totalOutflow.toCurrencyString(), XlsxStyle.LOSS)
        ))
        
        rows.add(emptyList()) // spacing
        rows.add(listOf(
            XlsxCell("Abertura do caixa", XlsxStyle.PROFIT),
            XlsxCell("", XlsxStyle.NORMAL),
            XlsxCell(session.initialAmount.toCurrencyString(), XlsxStyle.PROFIT)
        ))
        
        rows.add(listOf(
            XlsxCell("Total em caixa", XlsxStyle.TITLE),
            XlsxCell("", XlsxStyle.NORMAL),
            XlsxCell(totalInBox.toCurrencyString(), XlsxStyle.NORMAL),
            XlsxCell("", XlsxStyle.NORMAL),
            XlsxCell("", XlsxStyle.NORMAL),
            XlsxCell(totalOutflow.toCurrencyString(), XlsxStyle.NORMAL)
        ))
        
        rows.add(listOf(
            XlsxCell("Total Retirada", XlsxStyle.LOSS),
            XlsxCell("", XlsxStyle.NORMAL),
            XlsxCell(totalOutflow.toCurrencyString(), XlsxStyle.NORMAL)
        ))
        
        rows.add(listOf(
            XlsxCell("Fechamento do caixa", netBalanceStyle),
            XlsxCell("", XlsxStyle.NORMAL),
            XlsxCell(netBalance.toCurrencyString(), netBalanceStyle)
        ))

        // Generate XLSX
        val xlsxWriter = XlsxWriter(sheetName = "Diário $dateStr", rows = rows)
        return xlsxWriter.generateXlsxRawBytes()
    }
}
