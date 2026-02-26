package filipe.guerreiro.domain.usecase

import filipe.guerreiro.domain.model.CashSession
import filipe.guerreiro.domain.model.Category
import filipe.guerreiro.domain.model.PaymentMethod
import filipe.guerreiro.domain.model.Transaction
import filipe.guerreiro.domain.model.TransactionType
import filipe.guerreiro.domain.model.toCurrencyString
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

class GenerateCsvUseCase {

    operator fun invoke(
        session: CashSession,
        transactions: List<Transaction>,
        categories: List<Category>,
        paymentMethods: List<PaymentMethod>,
        totalInflow: Long,
        totalOutflow: Long,
        currentBalance: Long
    ): String {
        val sb = StringBuilder()

        // 1. Write the Header for the session metadata
        sb.appendLine("Status do Caixa;Data de Abertura;Data de Fechamento;Saldo Inicial;Total Entradas;Total Saídas;Saldo Final")
        
        val openingDate = session.openingTimeStamp.toLocalDateTime(TimeZone.currentSystemDefault())
            .let { "${it.day.toString().padStart(2, '0')}/${it.month.number.toString().padStart(2, '0')}/${it.year} ${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}" }
        
        val closingDate = session.closingTimeStamp?.toLocalDateTime(TimeZone.currentSystemDefault())
            ?.let { "${it.day.toString().padStart(2, '0')}/${it.month.number.toString().padStart(2, '0')}/${it.year} ${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}" }
            ?: "Em aberto"

        sb.append(session.status).append(";")
        sb.append(openingDate).append(";")
        sb.append(closingDate).append(";")
        sb.append(session.initialAmount.toCurrencyString()).append(";")
        sb.append(totalInflow.toCurrencyString()).append(";")
        sb.append(totalOutflow.toCurrencyString()).append(";")
        sb.appendLine(currentBalance.toCurrencyString())

        // Add a blank line separator
        sb.appendLine()

        // 2. Write the Header for Transactions
        sb.appendLine("Data/Hora;Tipo;Categoria;Método de Pagamento;Valor;Descrição")

        // 3. Write each transaction row
        for (tx in transactions) {
            val dateTime = tx.timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
                .let { "${it.day.toString().padStart(2, '0')}/${it.month.number.toString().padStart(2, '0')}/${it.year} ${it.hour.toString().padStart(2, '0')}:${it.minute.toString().padStart(2, '0')}" }
            
            val type = if (tx.type == TransactionType.INCOME) "Entrada" else "Saída"
            
            val categoryName = categories.find { it.id == tx.categoryId }?.name ?: "Sem Categoria"
            val paymentMethodName = paymentMethods.find { it.id == tx.paymentMethodId }?.name ?: "Sem Método"
            
            val amount = tx.amount.toCurrencyString()

            // Escape description: remove semi-colons and newlines for CSV safety
            val description = tx.description
                .replace(";", ",")
                .replace("\n", " ")
                .replace("\"", "\"\"")
            
            // If we have commas or special characters, wrap but here we use semicolon delimiter.
            sb.append(dateTime).append(";")
            sb.append(type).append(";")
            sb.append(categoryName).append(";")
            sb.append(paymentMethodName).append(";")
            sb.append(amount).append(";")
            sb.appendLine("\"$description\"")
        }

        return sb.toString()
    }
}
