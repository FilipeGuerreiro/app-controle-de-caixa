package filipe.guerreiro.ui.cash.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import filipe.guerreiro.ui.theme.financial
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon

// ----- Sub-tab enum -----

private enum class SummaryTab(val label: String) {
    CATEGORIES("Categorias"),
    PAYMENT_METHODS("Métodos de Pgto.")
}

// ----- Main Section -----

@Composable
fun SummaryBreakdownSection(
    categoryBalances: List<BreakdownItemUi>,
    paymentMethodBalances: List<BreakdownItemUi>,
    onManageCategories: () -> Unit = {},
    onManagePaymentMethods: () -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(SummaryTab.CATEGORIES) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Resumos",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            TextButton(
                onClick = {
                    when (selectedTab) {
                        SummaryTab.CATEGORIES -> onManageCategories()
                        SummaryTab.PAYMENT_METHODS -> onManagePaymentMethods()
                    }
                }
            ) {
                Text(
                    text = "Gerenciar",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Toggle chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            SummaryTab.entries.forEach { tab ->
                FilterChip(
                    selected = tab == selectedTab,
                    onClick = { selectedTab = tab },
                    label = {
                        Text(
                            text = tab.label,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (tab == selectedTab) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }

        // Content based on selected tab
        val items = when (selectedTab) {
            SummaryTab.CATEGORIES -> categoryBalances
            SummaryTab.PAYMENT_METHODS -> paymentMethodBalances
        }

        if (items.isEmpty()) {
            Text(
                text = "Nenhum dado disponível",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            val incomes = items.filter { it.isIncome }.sortedByDescending { it.amountValue }
            val expenses = items.filter { !it.isIncome }.sortedByDescending { it.amountValue }

            if (incomes.isNotEmpty()) {
                BreakdownGroupCard(title = "Entradas", items = incomes, isIncome = true)
            }
            if (expenses.isNotEmpty()) {
                BreakdownGroupCard(title = "Saídas", items = expenses, isIncome = false)
            }
        }
    }
}

// ----- Group Card -----

@Composable
private fun BreakdownGroupCard(
    title: String,
    items: List<BreakdownItemUi>,
    isIncome: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) MaterialTheme.financial.profit else MaterialTheme.colorScheme.error
                )
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items.forEach { item ->
                    BreakdownItemRow(item, isIncome)
                }
            }
        }
    }
}

// ----- Item Row -----

@Composable
private fun BreakdownItemRow(
    item: BreakdownItemUi,
    isIncome: Boolean
) {
    val color = if (isIncome) MaterialTheme.financial.profit else MaterialTheme.colorScheme.error

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
            Text(
                text = item.amountFormatted,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            )
        }

        LinearProgressIndicator(
            progress = item.progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round
        )
    }
}
