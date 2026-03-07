package filipe.guerreiro.ui.reports.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import filipe.guerreiro.domain.model.toCurrencyString
import filipe.guerreiro.ui.reports.CategoryBreakdownUi

private val DonutColors = listOf(
    Color(0xFFEF5350), // Red
    Color(0xFFAB47BC), // Purple
    Color(0xFF42A5F5), // Blue
    Color(0xFF26A69A), // Teal
    Color(0xFFFFA726), // Orange
    Color(0xFF8D6E63), // Brown
    Color(0xFF66BB6A), // Green
    Color(0xFFEC407A), // Pink
    Color(0xFF5C6BC0), // Indigo
    Color(0xFF78909C), // Blue Grey
)

@Composable
fun ExpenseCategoryDonutChart(
    categories: List<CategoryBreakdownUi>,
    title: String = "Saídas por Categoria",
    modifier: Modifier = Modifier
) {
    if (categories.isEmpty()) return

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(categories) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(800))
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
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Donut
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val totalAmount = categories.sumOf { it.amount }
                    Canvas(modifier = Modifier.size(130.dp)) {
                        val strokeWidth = 28.dp.toPx()
                        val radius = (size.minDimension - strokeWidth) / 2
                        val topLeft = Offset(
                            (size.width - radius * 2) / 2,
                            (size.height - radius * 2) / 2
                        )
                        val arcSize = Size(radius * 2, radius * 2)

                        var startAngle = -90f
                        categories.forEachIndexed { index, cat ->
                            val sweep = (cat.percentage / 100f) * 360f * animationProgress.value
                            val color = DonutColors[index % DonutColors.size]
                            drawArc(
                                color = color,
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                            )
                            startAngle += sweep
                        }
                    }

                    // Center label
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = totalAmount.toCurrencyString(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Total",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.width(16.dp))

                // Legend
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEachIndexed { index, cat ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(
                                        DonutColors[index % DonutColors.size],
                                        CircleShape
                                    )
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = cat.name,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${cat.percentage}%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
