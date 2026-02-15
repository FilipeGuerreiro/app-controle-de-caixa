package filipe.guerreiro.ui.cash.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import filipe.guerreiro.ui.theme.financial

@Composable
fun DailySummaryCard(summary: CashSummaryUi) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RESUMO",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatusPill(text = summary.status)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                SummaryValueColumn(
                    modifier = Modifier.weight(1f),
                    label = "Saldo Inicial",
                    value = summary.initialAmount
                )
                SummaryValueColumn(
                    modifier = Modifier.weight(1f),
                    label = "Saldo Atual",
                    value = summary.currentBalance,
                    valueColor = if (summary.status == "Aberto") MaterialTheme.financial.profit else null
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Row(modifier = Modifier.fillMaxWidth()) {
                MovementIndicator(
                    modifier = Modifier.weight(1f),
                    label = "Entradas",
                    value = summary.totalInflow,
                    isIncome = true
                )
                MovementIndicator(
                    modifier = Modifier.weight(1f),
                    label = "Saídas",
                    value = summary.totalOutflow,
                    isIncome = false
                )
            }
        }
    }
}

@Composable
private fun SummaryValueColumn(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: Color? = null
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = valueColor ?: MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun StatusPill(text: String) {
    // Lógica de cores baseada no status
    val isClosed = text.lowercase() == "fechado"
    val backgroundColor = if (isClosed) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.financial.profitContainer
    val textColor = if (isClosed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.financial.profit

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge.copy(
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
private fun MovementIndicator(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    isIncome: Boolean
) {
    // Cores extraídas da sua implementação original para manter a consistência
    val accentColor = if (isIncome) MaterialTheme.financial.profit else MaterialTheme.colorScheme.error
    val iconBackground = if (isIncome) MaterialTheme.financial.profitContainer else MaterialTheme.colorScheme.errorContainer
    val icon = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            modifier = Modifier.size(32.dp),
            shape = CircleShape,
            color = iconBackground
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.padding(6.dp)
            )
        }
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = accentColor
            )
        }
    }
}