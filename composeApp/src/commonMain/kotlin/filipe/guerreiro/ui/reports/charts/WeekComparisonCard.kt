package filipe.guerreiro.ui.reports.charts

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import filipe.guerreiro.domain.model.toCurrencyString
import filipe.guerreiro.ui.reports.PeriodSummaryUi
import filipe.guerreiro.ui.theme.financial

@Composable
fun WeekComparisonCard(
    currentSummary: PeriodSummaryUi,
    previousSummary: PeriodSummaryUi,
    modifier: Modifier = Modifier
) {
    val currentProfit = currentSummary.totalInflow - currentSummary.totalOutflow
    val previousProfit = previousSummary.totalInflow - previousSummary.totalOutflow

    val variation = if (previousProfit != 0L) {
        ((currentProfit - previousProfit).toDouble() / previousProfit * 100).toInt()
    } else if (currentProfit > 0) {
        100
    } else {
        0
    }

    val isPositive = variation >= 0
    val profitColor = MaterialTheme.financial.profit
    val lossColor = MaterialTheme.colorScheme.error
    val neutralColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    val trendColor = when {
        variation > 0 -> profitColor
        variation < 0 -> lossColor
        else -> neutralColor
    }

    val trendIcon = when {
        variation > 0 -> Icons.AutoMirrored.Filled.TrendingUp
        variation < 0 -> Icons.AutoMirrored.Filled.TrendingDown
        else -> Icons.AutoMirrored.Filled.TrendingFlat
    }

    val variationText = when {
        variation > 0 -> "+$variation%"
        else -> "$variation%"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Comparação Semanal",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Current week
                Column {
                    Text(
                        text = "Esta Semana",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currentProfit.toCurrencyString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (currentProfit >= 0) profitColor else lossColor
                    )
                }

                // Trend indicator
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = trendIcon,
                        contentDescription = null,
                        tint = trendColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = variationText,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = trendColor
                    )
                }

                // Previous week
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Semana Anterior",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = previousProfit.toCurrencyString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
