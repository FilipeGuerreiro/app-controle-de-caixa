package filipe.guerreiro.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun QuickActionSection(
    actions: List<QuickActionUiModel>,
    onActionClick: (QuickActionUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Ações Rápidas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Assuming max 4 items for quick actions as per requirement
        val chunkedActions = actions.take(4).chunked(2)
        
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            chunkedActions.forEach { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    rowItems.forEach { action ->
                        QuickActionCard(
                            item = action,
                            onClick = { onActionClick(action) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // If row has only 1 item, add spacer to keep alignment if needed, 
                    // but weight(1f) on single item would stretch it. 
                    // If we want equal width columns, we need a placeholder.
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun Spacer(modifier: Modifier) {
    androidx.compose.foundation.layout.Spacer(modifier = modifier)
}