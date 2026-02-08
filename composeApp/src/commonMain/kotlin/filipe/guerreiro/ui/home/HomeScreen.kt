package filipe.guerreiro.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import filipe.guerreiro.ui.theme.ControleDeCaixaTheme
import androidx.compose.ui.graphics.vector.ImageVector
import org.koin.compose.viewmodel.koinViewModel

data class RecentActivity(
    val id: String,
    val title: String,
    val time: String,
    val type: String,
    val amount: Double,
    val isIncome: Boolean,
    val icon: ImageVector
)

data class QuickActionUiModel(
    val id: Int,
    val emoji: String,
    val title: String,
    val priceStr: String
)

val mockedQuickActions = listOf(
    QuickActionUiModel(1, "⬆️", "Vendas", "Pix"),
    QuickActionUiModel(2, "⬆️", "Venda", "Crédito"),
    QuickActionUiModel(3, "⬇️", "Saída", "Serviços"),
)

@Composable
fun HomeScreen(
    onOpenCashClick: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        HomeHeader(
            userName = "Flávia",
            businessName = "Loja Espetinhos"
        )

        Spacer(modifier = Modifier.height(24.dp))

        CashStatusCard(
            onOpenCashClick = onOpenCashClick
        )

        Spacer(modifier = Modifier.height(12.dp))

        QuickActionSection(
            actions = mockedQuickActions,
            onActionClick = { action ->
                println("Clickou em ${action.title} - Valor: ${action.priceStr}")
                viewModel.disableUserRegistered()
            }
        )

        Spacer(modifier = Modifier.height(12.dp))


        RecentActivities()
    }
}

@Composable
fun HomeHeader(
    userName: String,
    businessName: String
) {
    Column {
        Text(
            text = "Boas vindas, $userName.",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        )
        Text(
            text = businessName,
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Light,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onBackground
            ),
            modifier = Modifier.padding(start = 6.dp, top = 8.dp)
        )
    }
}

@Composable
fun CashStatusCard(
    onOpenCashClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Linha Superior: Ícone + Status + Seta
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Ícone circular cinza claro (fundo)
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Caixa Fechado",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Ir para detalhes",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botão Primário (Verde - Abrir Caixa)
                Text(
                    text = "Meta diária: R$ XXX/XXX | 80%",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

                Spacer(modifier = Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botão Primário (Verde - Abrir Caixa)
                Text(
                    text = "Entradas: R$ XXX",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = "Saídas: R$ XXX",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))


            // Linha Inferior: Botões de Ação
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Botão Primário (Verde - Abrir Caixa)
                Button(
                    onClick = onOpenCashClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LockOpen,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Abrir Caixa")
                }

                // Botão Secundário (Outline - Ver Detalhes)
                OutlinedButton(
                    onClick = { /* TODO: Ver Detalhes */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Ver Detalhes")
                }
            }
        }
    }
}

@Composable
fun RecentActivities() {
    val mockActivities = listOf(
        RecentActivity(
            id = "1",
            title = "Combo Meal #1",
            time = "1:45 PM",
            type = "Cash",
            amount = 12.50,
            isIncome = true,
            icon = Icons.Default.LocalDining
        ),
        RecentActivity(
            id = "2",
            title = "Soda (L)",
            time = "1:42 PM",
            type = "Card",
            amount = 3.00,
            isIncome = true,
            icon = Icons.Default.ShoppingCart
        ),
        RecentActivity(
            id = "3",
            title = "Ice Re-stock",
            time = "1:15 PM",
            type = "Inventory",
            amount = 8.00,
            isIncome = false,
            icon = Icons.Default.Inventory2
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 0.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Atividades Recentes",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            Text(
                text = "Ver todas",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(end = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            mockActivities.forEach { activity ->
                ActivityItem(activity = activity)
            }
        }
    }
}

@Composable
fun ActivityItem(activity: RecentActivity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ícone com fundo colorido
                val bgColor = if (activity.isIncome) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                } else {
                    MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                }
                val iconColor = if (activity.isIncome) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = bgColor,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = activity.icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier
                            .padding(8.dp)
                            .size(24.dp)
                    )
                }

                Column {
                    Text(
                        text = activity.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "${activity.time} • ${activity.type}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            // Valor com cor
            val amountColor = if (activity.isIncome) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            }
            val amountSign = if (activity.isIncome) "+" else "-"

            Text(
                text = "$amountSign$${activity.amount}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = amountColor
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    ControleDeCaixaTheme {
        HomeScreen(
            onOpenCashClick = {}
        )
    }
}
