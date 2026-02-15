package filipe.guerreiro.ui.cash

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import filipe.guerreiro.ui.theme.ControleDeCaixaTheme
import org.koin.compose.viewmodel.koinViewModel


@Composable
fun CashScreen(
    viewModel: CashViewModel = koinViewModel(),
    onNavigateToUserSelection: () -> Unit,
    onOpenCashClick: () -> Unit = {},
    onNavigateToTransaction: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn == false) {
            onNavigateToUserSelection()
        }
    }

    AnimatedContent(
        targetState = uiState.selectedSession,
        transitionSpec = {
            if (targetState != null) {
                slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
            } else {
                slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
            }
        },
        label = "CashScreenNavigation"
    ) { selectedSession ->
        if (selectedSession == null) {
            CashListContent(
                sessions = uiState.cashSessions,
                onSessionClick = viewModel::selectSession,
                onOpenCashClick = onOpenCashClick
            )
        } else {
            CashDetailsContent(
                session = selectedSession,
                historyItems = uiState.historyItems,
                onBackClick = viewModel::clearSelection,
                onPreviousClick = viewModel::previousSession,
                onNextClick = viewModel::nextSession,
                onNavigateToTransaction = onNavigateToTransaction
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashListContent(
    sessions: List<CashSessionUi>,
    onSessionClick: (CashSessionUi) -> Unit,
    onOpenCashClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Meus Caixas") }
            )
        },
        floatingActionButton = {
           // Provide a mechanism to open a new cash session if desired, 
           // here we reuse the existing callback potentially.
           // Or just leave it for the user to decide where to put "Open New Cash"
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sessions) { session ->
                CashSessionCard(session, onClick = { onSessionClick(session) })
            }
        }
    }
}

@Composable
fun CashSessionCard(session: CashSessionUi, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = session.date,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                StatusPill(text = session.status)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Saldo Final",
                    style = MaterialTheme.typography.labelSmall
                )
                Text(
                    text = session.finalBalance ?: "---",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFF0F6A37),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(modifier = Modifier.size(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashDetailsContent(
    session: CashSessionUi,
    historyItems: List<HistoryItemUi>,
    onBackClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onNavigateToTransaction: () -> Unit
) {
    Scaffold(
        topBar = {
            Column {
                // Main Top Bar with Back Button and Title
                TopAppBar(
                    title = { Text("Detalhes do Caixa") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar para lista")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Date Navigation Bar (Sub-header)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(onClick = onPreviousClick) {
                            Icon(Icons.Default.ChevronLeft, "Anterior")
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = session.date,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if(session.isCurrent) "Caixa Atual" else "Caixa Fechado",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }

                        IconButton(onClick = onNextClick) {
                            Icon(Icons.Default.ChevronRight, "Próximo")
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToTransaction,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Novo Lançamento")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DailySummaryCard()
            FilterChipsRow()
            // ✅ Otimização 3: Passamos a lista estática
            HistorySection(items = historyItems)
        }
    }
}

@Composable
private fun DailySummaryCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "RESUMO DO DIA",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                StatusPill(text = "Aberto")
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryColumn(
                    label = "Saldo Inicial",
                    value = "R$ 150,00",
                    valueColor = MaterialTheme.colorScheme.onSurface
                )
                SummaryColumn(
                    label = "Saldo Atual",
                    value = "R$ 582,50",
                    valueColor = Color(0xFF0F6A37)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            DividerLight()
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryDirection(
                    label = "Entradas",
                    value = "+ R$ 680,00",
                    accentColor = Color(0xFF0F6A37),
                    icon = Icons.Default.ArrowUpward,
                    iconBackground = Color(0xFFDFF5E7)
                )
                SummaryDirection(
                    label = "Saídas",
                    value = "- R$ 247,50",
                    accentColor = Color(0xFFC62828),
                    icon = Icons.Default.ArrowDownward,
                    iconBackground = Color(0xFFFDE8E8)
                )
            }
        }
    }
}

@Composable
private fun SummaryColumn(
    label: String,
    value: String,
    valueColor: Color
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
                color = valueColor
            )
        )
    }
}

@Composable
private fun SummaryDirection(
    label: String,
    value: String,
    accentColor: Color,
    icon: ImageVector,
    iconBackground: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(20.dp),
            color = iconBackground
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier
                    .padding(8.dp)
                    .size(20.dp)
            )
        }
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            )
        }
    }
}

@Composable
private fun StatusPill(text: String) {
    // Check status to determine color
    val isClosed = text.lowercase() == "fechado"
    val backgroundColor = if(isClosed) Color(0xFFEEEEEE) else Color(0xFFDFF5E7)
    val textColor = if(isClosed) Color(0xFF616161) else Color(0xFF0F6A37)

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge.copy(
                color = textColor,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

@Composable
private fun DividerLight() {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    )
}

@Composable
private fun FilterChipsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FilterChip(
            label = "Tudo",
            selected = true
        )
        FilterChip(
            label = "Entradas",
            selected = false
        )
        FilterChip(
            label = "Saídas",
            selected = false
        )
        FilterChip(
            label = "Pix",
            selected = false
        )
    }
}


@Composable
private fun HistorySection(items: List<HistoryItemUi>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Histórico",
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )

        items.forEach { item ->
            HistoryCard(item = item)
        }
    }
}

@Composable
private fun HistoryCard(item: HistoryItemUi) {
    // Cálculo de cores extraído para facilitar leitura e debug
    val accentColor = if (item.isIncome) Color(0xFF0F6A37) else Color(0xFFC62828)
    val iconBackground = if (item.isIncome) Color(0xFFE8F4EE) else Color(0xFFFDE8E8)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(22.dp),
                    color = iconBackground
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(22.dp)
                    )
                }

                Column(
                    modifier = Modifier.wrapContentHeight(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "${item.time} • ${item.method}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = item.amountLabel,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                )
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    selected: Boolean
) {
    val background = if (selected) Color(0xFF0E5A37) else Color.Transparent
    val content = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
    val border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline)

    Button(
        onClick = { },
        colors = ButtonDefaults.buttonColors(
            containerColor = background,
            contentColor = content
        ),
        border = border,
        shape = RoundedCornerShape(999.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Medium)
        )
    }
}

