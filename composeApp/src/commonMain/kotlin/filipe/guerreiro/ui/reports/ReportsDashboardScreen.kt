package filipe.guerreiro.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import filipe.guerreiro.domain.model.toCurrencyString
import filipe.guerreiro.ui.theme.financial
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsDashboardScreen(
    cashId: Long? = null,
    onBackClick: () -> Unit
) {
    val viewModel: ReportsDashboardViewModel = koinViewModel { parametersOf(cashId) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Painel Financeiro", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (cashId != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                        }
                    }
                },
                actions = {
                    if (uiState.isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        IconButton(onClick = { viewModel.showExportSheet() }) {
                            Icon(
                                imageVector = Icons.Default.FileDownload,
                                contentDescription = "Exportar Relatório",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp) // Space for bottom nav or general padding
                ) {
                    item {
                        SegmentSelector(
                            selected = uiState.selectedSegment,
                            onSelect = { viewModel.onSegmentChanged(it) }
                        )
                    }

                    if (uiState.selectedSegment == ReportSegment.WEEKLY) {
                        item {
                            WeeklyPeriodSelector(
                                periods = uiState.weeklyPeriods,
                                selected = uiState.selectedWeeklyPeriod,
                                onSelect = { viewModel.onWeeklyPeriodSelected(it) }
                            )
                        }
                    } else if (uiState.preSelectedCashId == null) {
                        item {
                            DailySessionSelector(
                                sessions = uiState.dailySessions,
                                selected = uiState.selectedDailySession,
                                onSelect = { viewModel.onDailySessionSelected(it) }
                            )
                        }
                    }

                    item {
                        val summary = if (uiState.selectedSegment == ReportSegment.DAILY) uiState.dailySummary else uiState.weeklySummary
                        val label = if (uiState.selectedSegment == ReportSegment.DAILY) uiState.dailyDateLabel else uiState.selectedWeeklyPeriod?.label ?: ""
                        
                        DashboardHero(
                            summary = summary,
                            dateLabel = label,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    item {
                        val methods = if (uiState.selectedSegment == ReportSegment.DAILY) uiState.dailyPaymentMethods else uiState.weeklyPaymentMethods
                        if (methods.isNotEmpty()) {
                            PaymentMethodsProgressSection(
                                methods = methods,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }

                    // === CHARTS & INSIGHTS ===
                    if (uiState.selectedSegment == ReportSegment.DAILY) {
                        // Donut de saídas por categoria (Diário)
                        if (uiState.dailyExpensesByCategory.isNotEmpty()) {
                            item {
                                filipe.guerreiro.ui.reports.charts.ExpenseCategoryDonutChart(
                                    categories = uiState.dailyExpensesByCategory,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                        // Gauge de meta diária
                        uiState.dailyGoalProgress?.let { goalProgress ->
                            item {
                                filipe.guerreiro.ui.reports.charts.GoalProgressIndicator(
                                    progress = goalProgress,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    } else {
                        // Comparação com semana anterior
                        uiState.previousWeekSummary?.let { prevSummary ->
                            item {
                                filipe.guerreiro.ui.reports.charts.WeekComparisonCard(
                                    currentSummary = uiState.weeklySummary,
                                    previousSummary = prevSummary,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                        // Barras comparativas Entradas vs Saídas por dia
                        if (uiState.weeklyDailyAggregates.isNotEmpty()) {
                            item {
                                filipe.guerreiro.ui.reports.charts.DailyComparisonBarChart(
                                    aggregates = uiState.weeklyDailyAggregates,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                        // Donut de saídas por categoria (Semanal)
                        if (uiState.weeklyExpensesByCategory.isNotEmpty()) {
                            item {
                                filipe.guerreiro.ui.reports.charts.ExpenseCategoryDonutChart(
                                    categories = uiState.weeklyExpensesByCategory,
                                    title = "Saídas por Categoria (Semana)",
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    item {
                        Text(
                            text = if (uiState.selectedSegment == ReportSegment.DAILY) "Transações do Dia" else "Resumo dos Dias",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp, end = 16.dp)
                        )
                    }

                    if (uiState.selectedSegment == ReportSegment.DAILY) {
                        if (uiState.dailyTransactions.isEmpty()) {
                            item {
                                EmptyStateMessage("Nenhuma transação encontrada neste dia.")
                            }
                        } else {
                            items(uiState.dailyTransactions) { tx ->
                                TransactionListItem(tx)
                            }
                        }
                    } else {
                        if (uiState.weeklyDailyAggregates.isEmpty()) {
                            item {
                                EmptyStateMessage("Nenhum movimento nesta semana.")
                            }
                        } else {
                            items(uiState.weeklyDailyAggregates) { agg ->
                                DailyAggregateItem(agg)
                            }
                        }
                    }
                }
            }
        }
    }

    // Export Confirmation Bottom Sheet
    if (uiState.showExportSheet) {
        val segmentLabel = if (uiState.selectedSegment == ReportSegment.DAILY) "Diário" else "Semanal"
        val periodLabel = if (uiState.selectedSegment == ReportSegment.DAILY) {
            uiState.dailyDateLabel.ifEmpty { uiState.selectedDailySession?.label ?: "" }
        } else {
            uiState.selectedWeeklyPeriod?.label ?: ""
        }

        ModalBottomSheet(
            onDismissRequest = { viewModel.dismissExportSheet() },
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Exportar Relatório $segmentLabel",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                if (periodLabel.isNotEmpty()) {
                    Text(
                        text = "Período: $periodLabel",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "A planilha (.xlsx) será gerada e compartilhada automaticamente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Button(
                    onClick = { viewModel.exportCurrentView() },
                    enabled = !uiState.isExporting,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState.isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Gerando...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Gerar Planilha")
                    }
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SegmentSelector(selected: ReportSegment, onSelect: (ReportSegment) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SegmentButton(
            text = "Diário",
            isSelected = selected == ReportSegment.DAILY,
            modifier = Modifier.weight(1f),
            onClick = { onSelect(ReportSegment.DAILY) }
        )
        SegmentButton(
            text = "Semanal",
            isSelected = selected == ReportSegment.WEEKLY,
            modifier = Modifier.weight(1f),
            onClick = { onSelect(ReportSegment.WEEKLY) }
        )
    }
}

@Composable
fun SegmentButton(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    val border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f))
    
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(100.dp),
        border = border,
        modifier = modifier.height(40.dp),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, style = MaterialTheme.typography.labelLarge, color = textColor, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyPeriodSelector(
    periods: List<filipe.guerreiro.domain.usecase.WeeklyPeriodUi>,
    selected: filipe.guerreiro.domain.usecase.WeeklyPeriodUi?,
    onSelect: (filipe.guerreiro.domain.usecase.WeeklyPeriodUi) -> Unit
) {
    var expanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = selected?.label ?: "Selecione uma semana",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            periods.forEach { period ->
                DropdownMenuItem(
                    text = { Text(period.label) },
                    onClick = {
                        onSelect(period)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailySessionSelector(
    sessions: List<DailySessionUi>,
    selected: DailySessionUi?,
    onSelect: (DailySessionUi) -> Unit
) {
    var expanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        OutlinedTextField(
            value = selected?.label ?: "Selecione um caixa/dia",
            onValueChange = {},
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            sessions.forEach { session ->
                DropdownMenuItem(
                    text = { Text(session.label) },
                    onClick = {
                        onSelect(session)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun DashboardHero(summary: PeriodSummaryUi, dateLabel: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Lucro Líquido",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            val netProfit = summary.totalInflow - summary.totalOutflow
            val isPositive = netProfit >= 0
            val profitColor = if (isPositive) MaterialTheme.financial.profit else MaterialTheme.colorScheme.error
            
            Text(
                text = netProfit.toCurrencyString(),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = profitColor,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            if (dateLabel.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = formatDateLabel(dateLabel),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SummaryMiniCard("Vendas", summary.totalInflow, MaterialTheme.financial.profit, true)
                SummaryMiniCard("Retiradas", summary.totalOutflow, MaterialTheme.colorScheme.error, false)
            }
            
            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            Spacer(Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "Abertura", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = summary.initialAmount.toCurrencyString(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "Total em Caixa", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(text = summary.netBalance.toCurrencyString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun SummaryMiniCard(title: String, amount: Long, color: androidx.compose.ui.graphics.Color, isIncome: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(text = title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(4.dp))
        Text(text = amount.toCurrencyString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun PaymentMethodsProgressSection(methods: List<PaymentBreakdownUi>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Entradas por Método",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            methods.forEachIndexed { index, pm ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pm.name,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${pm.percentage}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    
                    val progress = pm.percentage / 100f
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionListItem(tx: TransactionUi) {
    val color = if (tx.isIncome) MaterialTheme.financial.profit else MaterialTheme.colorScheme.error
    val icon = if (tx.isIncome) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.15f),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.padding(8.dp))
        }
        
        Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
            Text(text = tx.title.ifEmpty { "Sem título" }, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            val sub = listOfNotNull(tx.categoryName, tx.paymentMethodName).joinToString(" • ")
            Text(text = sub, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(text = tx.amount.toCurrencyString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
            Text(text = tx.time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun DailyAggregateItem(agg: DailyAggregateUi) {
    val profit = agg.inflow - agg.outflow
    val isPositive = profit >= 0
    val color = if (isPositive) MaterialTheme.financial.profit else MaterialTheme.colorScheme.error
    
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = formatDateLabel(agg.dateLabel),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ArrowUpward, null, tint = MaterialTheme.financial.profit, modifier = Modifier.size(12.dp))
                        Text(agg.inflow.toCurrencyString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ArrowDownward, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(12.dp))
                        Text(agg.outflow.toCurrencyString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Lucro",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = profit.toCurrencyString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

private fun formatDateLabel(isoDate: String): String {
    return try {
        val parts = isoDate.split("-")
        "${parts[2]}/${parts[1]}/${parts[0]}"
    } catch (e: Exception) {
        isoDate
    }
}

@Composable
fun EmptyStateMessage(message: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Text(text = message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}
