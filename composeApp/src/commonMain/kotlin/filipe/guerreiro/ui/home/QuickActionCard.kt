package filipe.guerreiro.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import filipe.guerreiro.domain.model.TransactionType
import filipe.guerreiro.ui.components.CurrencyAmountInput
import filipe.guerreiro.ui.theme.financial

@Composable
fun QuickActionCard(
    item: QuickActionUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
){
    val isIncome = item.type == TransactionType.INCOME
    val color = if (isIncome) MaterialTheme.financial.profit else MaterialTheme.colorScheme.error
    val icon = getIconForPaymentMethod(item.paymentMethodName)

    OutlinedCard(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(),
            // Remove fixed height constraint or adjust if needed, but Column layout will dictate height
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Icon + Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Icon Badge
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            color = color.copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Type Indicator
                Text(
                    text = if (isIncome) "Entrada" else "Saída",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = color,
                    modifier = Modifier
                        .background(
                            color = color.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Column: Texts
            Column {
                Text(
                    text = item.categoryName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.paymentMethodName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun QuickActionAmountDialog(
    item: QuickActionUiModel,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var amountInCents by remember { mutableStateOf(0L) }
    val isIncome = item.type == TransactionType.INCOME
    val color = if (isIncome) MaterialTheme.financial.profit else MaterialTheme.colorScheme.error

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Novo Lançamento",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "${item.categoryName} • ${item.paymentMethodName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                CurrencyAmountInput(
                    amountInCents = amountInCents,
                    onAmountChange = { amountInCents = it },
                    textColor = color,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (amountInCents > 0) {
                        onConfirm(amountInCents)
                    }
                },
                enabled = amountInCents > 0
            ) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun getIconForPaymentMethod(name: String): ImageVector {
    return when {
        name.contains("Pix", ignoreCase = true) -> Icons.Default.QrCode
        name.contains("Dinheiro", ignoreCase = true) -> Icons.Default.AttachMoney
        name.contains("Cartão", ignoreCase = true) -> Icons.Default.CreditCard
        else -> Icons.Default.Wallet
    }
}