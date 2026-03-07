package filipe.guerreiro.ui.reports.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import filipe.guerreiro.ui.reports.DailyAggregateUi
import filipe.guerreiro.ui.theme.financial

@Composable
fun DailyComparisonBarChart(
    aggregates: List<DailyAggregateUi>,
    modifier: Modifier = Modifier
) {
    if (aggregates.isEmpty()) return

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(aggregates) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(800))
    }

    val profitColor = MaterialTheme.financial.profit
    val expenseColor = MaterialTheme.colorScheme.error
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

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
                text = "Entradas vs Saídas por Dia",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            // Legend
            Row(
                modifier = Modifier.padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                LegendItem(color = profitColor, label = "Entradas")
                LegendItem(color = expenseColor, label = "Saídas")
            }

            val maxValue = aggregates.maxOf { maxOf(it.inflow, it.outflow) }
            if (maxValue <= 0) return@Column

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                val barGroupCount = aggregates.size
                val chartHeight = size.height - 24.dp.toPx() // Space for labels
                val groupWidth = size.width / barGroupCount
                val barWidth = (groupWidth * 0.35f).coerceAtMost(40.dp.toPx())
                val gap = 3.dp.toPx()
                val cornerRadius = CornerRadius(4.dp.toPx())

                aggregates.forEachIndexed { index, agg ->
                    val groupCenter = groupWidth * index + groupWidth / 2

                    // Inflow bar (left)
                    val inflowHeight = (agg.inflow.toFloat() / maxValue) * chartHeight * animationProgress.value
                    drawRoundRect(
                        color = profitColor,
                        topLeft = Offset(
                            x = groupCenter - barWidth - gap / 2,
                            y = chartHeight - inflowHeight
                        ),
                        size = Size(barWidth, inflowHeight),
                        cornerRadius = cornerRadius
                    )

                    // Outflow bar (right)
                    val outflowHeight = (agg.outflow.toFloat() / maxValue) * chartHeight * animationProgress.value
                    drawRoundRect(
                        color = expenseColor,
                        topLeft = Offset(
                            x = groupCenter + gap / 2,
                            y = chartHeight - outflowHeight
                        ),
                        size = Size(barWidth, outflowHeight),
                        cornerRadius = cornerRadius
                    )
                }
            }

            // Day labels below chart
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                aggregates.forEach { agg ->
                    val shortLabel = formatShortDate(agg.dateLabel)
                    Text(
                        text = shortLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(10.dp)) {
            drawCircle(color = color)
        }
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatShortDate(dateLabel: String): String {
    // Input is ISO format "2026-03-05", output "05/03"
    return try {
        val parts = dateLabel.split("-")
        "${parts[2]}/${parts[1]}"
    } catch (e: Exception) {
        dateLabel
    }
}
