package filipe.guerreiro.ui.cash.listing

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private val filterOptions = listOf(
    CashTabFilter.ALL to "Todos",
    CashTabFilter.PROFIT to "Com Lucro",
    CashTabFilter.LOSS to "Com Prejuízo"
)

@Composable
fun CashListFilterBar(
    selectedFilter: CashTabFilter,
    onFilterSelected: (CashTabFilter) -> Unit,
    onClearFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFiltered = selectedFilter != CashTabFilter.ALL

    // Reorder: selected chip goes to the front (after ALL which is always first)
    val sortedOptions = if (isFiltered) {
        val selected = filterOptions.first { it.first == selectedFilter }
        val rest = filterOptions.filter { it.first != selectedFilter && it.first != CashTabFilter.ALL }
        val all = filterOptions.first { it.first == CashTabFilter.ALL }
        listOf(all, selected) + rest
    } else {
        filterOptions
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            sortedOptions.forEach { (tab, label) ->
                val isSelected = tab == selectedFilter

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected && tab != CashTabFilter.ALL) {
                            // Clicking the active chip deselects it (goes back to ALL)
                            onFilterSelected(CashTabFilter.ALL)
                        } else {
                            onFilterSelected(tab)
                        }
                    },
                    label = {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        )
                    },
                    leadingIcon = if (isSelected && tab != CashTabFilter.ALL) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        enabled = true,
                        selected = isSelected
                    )
                )
            }
        }

        // Persistent "clear filter" indicator when a filter is active
        AnimatedVisibility(
            visible = isFiltered,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filtro ativo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onClearFilter) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Limpar filtro",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = "Limpar",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
