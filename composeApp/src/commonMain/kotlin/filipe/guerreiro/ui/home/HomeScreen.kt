package filipe.guerreiro.ui.home

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WavingHand
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import filipe.guerreiro.domain.model.toCurrencyString
import filipe.guerreiro.ui.components.QuickActionSectionSkeleton
import filipe.guerreiro.ui.components.RecentActivitiesSkeleton
import filipe.guerreiro.ui.components.SkeletonBox
import filipe.guerreiro.ui.components.SkeletonCircle
import filipe.guerreiro.ui.theme.ControleDeCaixaTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    onOpenCashClick: () -> Unit,
    onCloseCashClick: () -> Unit,
    onNavigateToCash: (Long?) -> Unit,
    onNavigateToUserSelection: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Redireciona para seleção de usuário se não estiver logado
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn == false) {
            onNavigateToUserSelection()
        }
    }

    val showSkeleton = uiState.isLoading || uiState.isLoggedIn == null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        HomeHeader(
            userName = uiState.userName.ifEmpty { "Usuário" },
            businessName = uiState.businessName.ifEmpty { "Seu Negócio" },
            isLoading = showSkeleton
        )

        Spacer(modifier = Modifier.height(24.dp))

        CashStatusCard(
            isLoading = showSkeleton,
            isCashOpen = uiState.isCashOpen,
            isFirstAccess = uiState.isFirstAccess,
            dailyGoal = uiState.dailyGoal,
            totalIncome = uiState.totalIncome,
            totalExpense = uiState.totalExpense,
            onOpenCashClick = onOpenCashClick,
            onCloseCashClick = onCloseCashClick,
            onNavigateToCash = { onNavigateToCash(uiState.currentCashId) }
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Actions com Skeleton Loading encapsulado
        val showEmptyMessageQuickAction = !showSkeleton && uiState.quickActions.isEmpty()

        if (showSkeleton) {
            QuickActionSectionSkeleton(itemCount = 4)
        } else if (uiState.quickActions.isNotEmpty()) {
            QuickActionSection(
                actions = uiState.quickActions,
                onActionClick = { action ->
                    println("Clickou em ${action.title} - Valor: ${action.priceStr}")
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Recent Activities com Skeleton Loading encapsulado
        val showEmptyMessageActivities = !showSkeleton && uiState.recentActivities.isEmpty()

        if (showSkeleton) {
            RecentActivitiesSkeleton(itemCount = 3)
        } else if (uiState.recentActivities.isNotEmpty()) {
            RecentActivities(activities = uiState.recentActivities)
        }

        // Mensagem de estado vazio quando não há dados
        if (showEmptyMessageQuickAction && showEmptyMessageActivities && !uiState.isFirstAccess) {
            EmptyStateMessage()
        }
    }
}

@Composable
fun FirstAccessCard(
    onOpenCashClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ícone com gradiente visual
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(80.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AttachMoney,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(20.dp)
                        .size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Comece criando seu primeiro caixa para registrar as movimentações do seu negócio.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onOpenCashClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 2.dp,
                    pressedElevation = 0.dp
                )
            ) {
                Text(
                    text = "Criar Primeiro Caixa",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
fun EmptyStateMessage(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = RoundedCornerShape(50),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddBusiness,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Comece a usar o caixa!",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Registre suas primeiras entradas e saídas para visualizar ações rápidas e atividades recentes aqui.",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(horizontal = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun HomeHeader(
    userName: String,
    businessName: String,
    isLoading: Boolean = false
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Boas vindas, ",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            )
            if (isLoading) {
                SkeletonBox(width = 100.dp, height = 24.dp)
            } else {
                Text(
                    text = "$userName.",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (isLoading) {
            SkeletonBox(width = 150.dp, height = 20.dp)
        } else {
            Text(
                text = businessName,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Light,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                ),
                modifier = Modifier.padding(start = 6.dp)
            )
        }
    }
}

@Composable
fun CashStatusCard(
    isLoading: Boolean = false,
    isCashOpen: Boolean = false,
    isFirstAccess: Boolean = false,
    dailyGoal: String = "",
    totalIncome: String = "",
    totalExpense: String = "",
    onOpenCashClick: () -> Unit,
    onCloseCashClick: () -> Unit,
    onNavigateToCash: () -> Unit
) {
    // Se for primeiro acesso, exibe card introdutório
    if (isFirstAccess && !isLoading) {
        FirstAccessCard(onOpenCashClick = onOpenCashClick)
        return
    }
    
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
                if (isLoading) {
                    SkeletonCircle()
                    Spacer(modifier = Modifier.width(12.dp))
                    SkeletonBox(width = 200.dp, height = 24.dp)
                } else {
                    // Ícone muda conforme status do caixa
                    val statusIcon = if (isCashOpen) Icons.Default.LockOpen else Icons.Default.Lock
                    val statusColor = if (isCashOpen) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = statusColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = statusIcon,
                            contentDescription = null,
                            tint = statusColor,
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = if (isCashOpen) "Caixa Aberto" else "Caixa Fechado",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Ir para detalhes",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Meta diária (mockada)
            if (isLoading) {
                SkeletonBox(width = 200.dp, height = 24.dp)
            } else {
                Text(
                    text = "Meta diária: $dailyGoal",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Entradas e Saídas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isLoading) {
                    SkeletonBox(modifier = Modifier.weight(1f), height = 24.dp)
                    SkeletonBox(modifier = Modifier.weight(1f), height = 24.dp)
                } else {
                    Text(
                        text = "Entradas: $totalIncome",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = "Saídas: $totalExpense",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botões de Ação
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isLoading) {
                    SkeletonBox(Modifier.weight(1f), height = 44.dp, shape = RoundedCornerShape(8.dp))
                    SkeletonBox(Modifier.weight(1f), height = 44.dp, shape = RoundedCornerShape(8.dp))
                } else {
                    // Botão principal muda conforme status
                    Button(
                        onClick = if (isCashOpen) onCloseCashClick else onOpenCashClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCashOpen) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = if (isCashOpen) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = if (isCashOpen) "Fechar Caixa" else "Abrir Caixa")
                    }

                    OutlinedButton(
                        onClick = onNavigateToCash,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = BorderStroke(
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
}

@Composable
fun RecentActivities(activities: List<RecentActivity>) {
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
            activities.forEach { activity ->
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

            // Valor formatado com cor
            val amountColor = if (activity.isIncome) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.error
            }
            val amountSign = if (activity.isIncome) "+" else "-"

            Text(
                text = "$amountSign${activity.amount.toCurrencyString()}",
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
            onOpenCashClick = {},
            onCloseCashClick = {},
            onNavigateToCash = {},
            onNavigateToUserSelection = {}
        )
    }
}
