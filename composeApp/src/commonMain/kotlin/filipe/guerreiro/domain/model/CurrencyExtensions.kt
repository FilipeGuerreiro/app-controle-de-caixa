package filipe.guerreiro.domain.model

import kotlin.math.abs
import kotlin.math.round

/**
 * Converte um valor em centavos (`Long`) para uma String formatada de moeda no formato brasileiro.
 * Exemplo: `1250L.toCurrencyString()` -> "R$ 12,50"
 */
fun Long.toCurrencyString(): String {
    val negative = this < 0
    val absValue = abs(this)
    val reais = absValue / 100
    val cents = absValue % 100
    val centsStr = cents.toString().padStart(2, '0')
    val prefix = if (negative) "-R$ " else "R$ "
    return "$prefix$reais,$centsStr"
}

fun Long.toCurrencyStringWithoutPrefix(): String {
    val absValue = abs(this)
    val reais = absValue / 100
    val cents = absValue % 100
    val centsStr = cents.toString().padStart(2, '0')
    return "$reais,$centsStr"
}

/**
 * Converte um `Double` representando reais para centavos (`Long`).
 * Exemplo: `12.5.toCents()` -> 1250L
 */
fun Double.toCents(): Long = round(this * 100).toLong()

/**
 * Tenta converter uma `String` digitada pelo usuário para centavos (`Long`).
 * Aceita strings como "R$ 12,50", "12.50", "12,50" ou "12".
 */
fun String.toCents(): Long {
    if (this.isBlank()) return 0L
    val cleaned = this.replace("R$", "").replace("\u00A0", "").replace(",", ".").trim()
    val doubleValue = cleaned.toDoubleOrNull() ?: 0.0
    return round(doubleValue * 100).toLong()
}
