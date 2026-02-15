package filipe.guerreiro.ui.cash.listing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import filipe.guerreiro.ui.theme.financial

@Composable
fun CashSessionGridCard(
    session: CashSessionUi,
    onClick: () -> Unit
) {
    val isClosed = session.status.lowercase() == "fechado"

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp), // Altura fixa para manter o alinhamento no grid
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = session.date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                StatusPillSmall(isClosed = isClosed)
            }

            Column {
                Text(
                    text = "Saldo Final",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = session.finalBalance ?: "---",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isClosed) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.financial.profit // Cor de destaque para caixa aberto
                    )
                )
            }
        }
    }
}

@Composable
private fun StatusPillSmall(isClosed: Boolean) {
    val backgroundColor = if (isClosed) MaterialTheme.colorScheme.surfaceVariant
    else MaterialTheme.colorScheme.primaryContainer
    val textColor = if (isClosed) MaterialTheme.colorScheme.onSurfaceVariant
    else MaterialTheme.colorScheme.onPrimaryContainer

    Surface(
        color = backgroundColor,
        shape = MaterialTheme.shapes.extraSmall
    ) {
        Text(
            text = if (isClosed) "Fechado" else "Aberto",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        )
    }
}