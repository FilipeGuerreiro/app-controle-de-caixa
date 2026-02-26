package filipe.guerreiro.domain.model

import kotlin.math.abs
import kotlin.math.round

/**
 * Converte um valor em centavos (`Long`) para uma String formatada de moeda no formato brasileiro.
 * Exemplo: `1250L.toCurrencyString()` -> "R$ 12,50"
 * Exemplo: `1000000L.toCurrencyString()` -> "R$ 10.000,00"
 */
fun Long.toCurrencyString(): String {
    val negative = this < 0
    val absValue = abs(this)
    val reais = absValue / 100
    val cents = absValue % 100
    val centsStr = cents.toString().padStart(2, '0')
    val reaisStr = reais.formatWithThousandsSeparator()
    val prefix = if (negative) "-R$ " else "R$ "
    return "$prefix$reaisStr,$centsStr"
}

fun Long.toCurrencyStringWithoutPrefix(): String {
    val negative = this < 0
    val absValue = abs(this)
    val reais = absValue / 100
    val cents = absValue % 100
    val centsStr = cents.toString().padStart(2, '0')
    val reaisStr = reais.formatWithThousandsSeparator()
    val prefix = if (negative) "-" else ""
    return "$prefix$reaisStr,$centsStr"
}

private fun Long.formatWithThousandsSeparator(): String {
    val str = this.toString()
    val result = StringBuilder()
    var count = 0
    for (i in str.length - 1 downTo 0) {
        if (count > 0 && count % 3 == 0) {
            result.append('.')
        }
        result.append(str[i])
        count++
    }
    return result.reverse().toString()
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
    // Remove tudo que não for dígito
    val digitsOnly = this.filter { it.isDigit() }
    return digitsOnly.toLongOrNull() ?: 0L
}
