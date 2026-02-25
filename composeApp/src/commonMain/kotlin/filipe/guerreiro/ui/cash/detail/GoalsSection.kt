package filipe.guerreiro.ui.cash.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import filipe.guerreiro.ui.theme.financial

// ----- Mock Data -----

data class GoalUi(
    val title: String,
    val currentFormatted: String,
    val targetFormatted: String,
    val progress: Float, // 0.0 to 1.0
    val icon: ImageVector,
    val isAchieved: Boolean = false
)

val mockGoals = listOf(
    GoalUi(
        title = "Meta de Vendas",
        currentFormatted = "R$ 2.505,00",
        targetFormatted = "R$ 3.000,00",
        progress = 0.835f,
        icon = Icons.Default.Flag
    ),
    GoalUi(
        title = "Meta de Lucro",
        currentFormatted = "R$ 505,00",
        targetFormatted = "R$ 800,00",
        progress = 0.63f,
        icon = Icons.Default.TrendingUp
    ),
    GoalUi(
        title = "Nº de Transações",
        currentFormatted = "4",
        targetFormatted = "10",
        progress = 0.4f,
        icon = Icons.Default.EmojiEvents
    )
)

// ----- Composables -----

@Composable
fun GoalsSection(
    goals: List<GoalUi>,
    isOpen: Boolean,
    onGoalClick: (GoalUi) -> Unit = {},
    onAddGoalClick: () -> Unit = {}
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Metas",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        if (goals.isEmpty()) {
            Card(
                modifier = if (isOpen) Modifier.fillMaxWidth().clickable { onAddGoalClick() } else Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isOpen) "Você ainda não definiu\numa meta para este caixa." else "Nenhuma meta foi definida\npara este caixa.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        textAlign = TextAlign.Center
                    )
                    if (isOpen) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Toque aqui para adicionar uma meta diária.",
                            style = MaterialTheme.typography.labelMedium.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            goals.forEach { goal ->
                GoalCard(goal, isOpen = isOpen, onClick = { onGoalClick(goal) })
            }
        }
    }
}

@Composable
private fun GoalCard(goal: GoalUi, isOpen: Boolean, onClick: () -> Unit = {}) {
    val progressColor = when {
        goal.isAchieved || goal.progress >= 1f -> MaterialTheme.financial.profit
        goal.progress >= 0.7f -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.tertiary
    }

    Card(
        modifier = if (isOpen) Modifier.fillMaxWidth().clickable { onClick() } else Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Circular progress with icon in center
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(56.dp)
            ) {
                CircularProgressIndicator(
                    progress = { goal.progress },
                    modifier = Modifier.size(56.dp),
                    color = progressColor,
                    trackColor = progressColor.copy(alpha = 0.15f),
                    strokeWidth = 5.dp,
                    strokeCap = StrokeCap.Round
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = progressColor.copy(alpha = 0.15f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = goal.icon,
                        contentDescription = null,
                        tint = progressColor,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            // Info column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = goal.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = "${goal.currentFormatted} de ${goal.targetFormatted}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            // Percentage
            Text(
                text = "${(goal.progress * 100).toInt()}%",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = progressColor
                ),
                textAlign = TextAlign.End
            )
        }
    }
}
