package filipe.guerreiro.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            // O LazyGrid precisa de altura definida se estiver dentro de um Column rolável.
            // Para 4 itens (2 linhas), ~180dp funciona. Para 6 itens, aumente.
            // Uma alternativa melhor em telas reais é não usar LazyGrid dentro de Column rolável,
            // mas para este mock vamos fixar a altura.
            modifier = Modifier.height(180.dp),
            userScrollEnabled = false // Desabilita a rolagem interna do grid
        ) {
            items(actions) { action ->
                QuickActionCard(
                    item = action,
                    onClick = { onActionClick(action) }
                )
            }
        }
    }
}