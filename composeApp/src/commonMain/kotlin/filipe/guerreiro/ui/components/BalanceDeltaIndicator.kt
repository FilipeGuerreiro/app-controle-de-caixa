package filipe.guerreiro.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import filipe.guerreiro.domain.model.toCurrencyString
import filipe.guerreiro.ui.theme.financial

@Composable
fun BalanceDeltaIndicator(
    currentBalance: Long,
    initialBalance: Long,
    modifier: Modifier = Modifier
) {
    val delta = currentBalance - initialBalance
    
    // Se não houve movimento, não renderizamos nada ou poderíamos renderizar um neutro.
    // Aqui optamos por exibir R$0,00 ou simplesmente retornar
    if (delta == 0L) return

    val isPositive = delta >= 0
    val color = if (isPositive) MaterialTheme.financial.profit else MaterialTheme.colorScheme.error
    val icon = if (isPositive) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
    val sign = if (isPositive) "+" else "-"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = if (isPositive) "Lucro" else "Prejuízo",
            tint = color,
            modifier = Modifier.width(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$sign${kotlin.math.abs(delta).toCurrencyString()}",
            maxLines = 1,
            softWrap = false,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        )
    }
}
