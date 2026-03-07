package filipe.guerreiro.ui.cash.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import filipe.guerreiro.domain.usecase.WeeklyPeriodUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyPeriodBottomSheet(
    periods: List<WeeklyPeriodUi>,
    isExporting: Boolean,
    onDismiss: () -> Unit,
    onExport: (WeeklyPeriodUi) -> Unit
) {
    val sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selected by remember { mutableStateOf<WeeklyPeriodUi?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Selecionar Semana",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            if (periods.isEmpty()) {
                Text(
                    text = "Nenhuma sessão encontrada.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    items(periods) { period ->
                        val isSelected = selected == period
                        PeriodRow(
                            period = period,
                            isSelected = isSelected,
                            onClick = { selected = period }
                        )
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { selected?.let { onExport(it) } },
                    enabled = selected != null && !isExporting,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF43A047)
                    )
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.size(6.dp))
                    Text(if (isExporting) "Gerando Planilha..." else "Gerar Planilha")
                }
            }
        }
    }
}

@Composable
private fun PeriodRow(
    period: WeeklyPeriodUi,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        else
            Color.Transparent,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RadioButton(selected = isSelected, onClick = onClick)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = period.label,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium)
                )
                Text(
                    text = "${period.sessionCount} ${if (period.sessionCount == 1) "sessão" else "sessões"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
