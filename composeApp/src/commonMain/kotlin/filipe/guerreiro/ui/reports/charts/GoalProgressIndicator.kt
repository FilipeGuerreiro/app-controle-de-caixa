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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import filipe.guerreiro.domain.model.toCurrencyString
import filipe.guerreiro.ui.reports.GoalProgressUi
import filipe.guerreiro.ui.theme.financial

@Composable
fun GoalProgressIndicator(
    progress: GoalProgressUi,
    modifier: Modifier = Modifier
) {
    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(progress) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(1f, animationSpec = tween(1000))
    }

    val profitColor = MaterialTheme.financial.profit
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val exceededColor = MaterialTheme.colorScheme.primary
    val clampedPercentage = progress.percentage.coerceAtMost(100)
    val isExceeded = progress.percentage > 100

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Meta Diária",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            )

            Box(
                modifier = Modifier.size(160.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(150.dp)) {
                    val strokeWidth = 16.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val topLeft = Offset(
                        (size.width - radius * 2) / 2,
                        (size.height - radius * 2) / 2
                    )
                    val arcSize = Size(radius * 2, radius * 2)

                    // Track (full arc — 240° sweep from 150° start)
                    drawArc(
                        color = trackColor,
                        startAngle = 150f,
                        sweepAngle = 240f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Progress arc
                    val progressSweep = (clampedPercentage / 100f) * 240f * animationProgress.value
                    val arcColor = if (isExceeded) exceededColor else profitColor
                    drawArc(
                        color = arcColor,
                        startAngle = 150f,
                        sweepAngle = progressSweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${progress.percentage}%",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isExceeded) exceededColor else profitColor
                    )
                    Text(
                        text = "da meta",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Vendas",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = progress.currentInflow.toCurrencyString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Meta",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = progress.goalAmount.toCurrencyString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
