package filipe.guerreiro.domain.model

import androidx.room.TypeConverter
import kotlin.time.Instant
import kotlin.math.abs
import kotlin.math.round

class Converters {

    @TypeConverter
    fun fromTimeStamp(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMilliseconds(it)}
    }

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? {
        return date?.toEpochMilliseconds()
    }

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }

    @TypeConverter
    fun fromCashStatusType(value: CashStatusType): String {
        return value.name
    }

    @TypeConverter
    fun toCashStatusType(value: String): CashStatusType {
        return CashStatusType.valueOf(value)
    }

    // Extensão para converter Long (centavos) em String formatada para a UI
    // Implementação sem usar String.format para ser multiplataforma (commonMain)
    fun Long.toCurrencyString(): String {
        val negative = this < 0
        val absValue = abs(this)
        val reais = absValue / 100
        val cents = absValue % 100
        val centsStr = cents.toString().padStart(2, '0')
        val prefix = if (negative) "-R$ " else "R$ "
        return "$prefix$reais,$centsStr"
    }

    // Extensão para converter o que o usuário digita (Double/String) para Long (centavos)
    fun Double.toCents(): Long = round(this * 100).toLong()

    fun String.toCents(): Long {
        if (this.isBlank()) return 0L
        // Limpa a string de símbolos monetários e troca vírgula por ponto
        val cleaned = this.replace("R$", "").replace(",", ".").trim()
        val doubleValue = cleaned.toDoubleOrNull() ?: 0.0
        return kotlin.math.round(doubleValue * 100).toLong()
    }
}