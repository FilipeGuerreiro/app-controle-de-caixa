package filipe.guerreiro.ui.cash.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import androidx.compose.material.icons.filled.Info

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashDetailScreen(
    cashId: Long,
    onBackClick: () -> Unit,
    onNavigateToTransaction: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToPaymentMethods: () -> Unit,
    onOpenCashClick: () -> Unit,
    onCloseCashClick: () -> Unit
) {

    val viewModel: CashDetailViewModel = koinViewModel { parametersOf(cashId) }
    val uiState by viewModel.uiState.collectAsState()
    var isMinLoadingElapsed by remember { mutableStateOf(false) }
    var selectedSection by remember { mutableStateOf(DetailSection.HISTORY) }
    var showEditGoalDialog by remember { mutableStateOf(false) }
    var editingTransactionId by remember { mutableStateOf<String?>(null) }
    var showAuditLogsSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isMinLoadingElapsed = true
    }

    val showSkeleton = uiState.isLoading || !isMinLoadingElapsed

    Scaffold(
        topBar = {
            CashDetailTopBar(
                title = "Detalhes do Caixa",
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (showSkeleton) {
                    DailySummaryCardSkeleton()
                    HistorySectionSkeleton()
                } else {
                    if (uiState.auditLogs.isNotEmpty()) {
                        androidx.compose.material3.Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showAuditLogsSheet = true },
                            colors = androidx.compose.material3.CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            ),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ) {
                            androidx.compose.foundation.layout.Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                androidx.compose.material3.Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                androidx.compose.material3.Text(
                                    text = "Este caixa foi fechado mas possui ${uiState.auditLogs.size} modificação(ões) de auditoria retroativas.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }

                    DailySummaryCard(
                        summary = uiState.summary,
                        onOpenCashClick = onOpenCashClick,
                        onCloseCashClick = onCloseCashClick
                    )

                    SectionPickerGrid(
                        selectedSection = selectedSection,
                        onSectionSelected = { selectedSection = it }
                    )

                    when (selectedSection) {
                        DetailSection.SUMMARY -> {
                            SummaryBreakdownSection(
                                categoryBalances = uiState.categoryBalances,
                                paymentMethodBalances = uiState.paymentMethodBalances,
                                onManageCategories = onNavigateToCategories,
                                onManagePaymentMethods = onNavigateToPaymentMethods,
                                onAddTransaction = onNavigateToTransaction
                            )
                        }
                        DetailSection.HISTORY -> {
                            HistorySection(
                                items = uiState.historyItems,
                                isOpen = uiState.summary.status == "Aberto",
                                onAddTransaction = onNavigateToTransaction,
                                onTransactionClick = { viewModel.selectTransaction(it) }
                            )
                        }
                        DetailSection.GOALS -> {
                            GoalsSection(
                                goals = uiState.goals,
                                isOpen = uiState.summary.status == "Aberto",
                                onGoalClick = { showEditGoalDialog = true },
                                onAddGoalClick = { showEditGoalDialog = true }
                            )
                        }
                        DetailSection.EXPORT -> {
                            ReportsSection(
                                isExporting = uiState.isExporting,
                                onExportCsvClick = { viewModel.exportToCsv() }
                            )
                        }
                    }
                }
            }
        }
    }

    // Edit Goal Dialog
    if (showEditGoalDialog) {
        EditGoalDialog(
            currentGoalInCents = uiState.summary.dailyGoalAmount ?: 0L,
            onDismiss = { showEditGoalDialog = false },
            onConfirm = { newGoal ->
                viewModel.updateDailyGoal(newGoal)
                showEditGoalDialog = false
            }
        )
    }

    // Transaction Detail Bottom Sheet
    uiState.selectedTransaction?.let { transaction ->
        TransactionDetailBottomSheet(
            transaction = transaction,
            onDismiss = { viewModel.clearSelectedTransaction() },
            onEdit = {
                editingTransactionId = it
                viewModel.clearSelectedTransaction()
            },
            onDelete = {
                viewModel.requestTransactionDelete(it)
            }
        )
    }

    // Edit Dialog
    editingTransactionId?.let { id ->
        val rawTx = uiState.rawTransactions.find { it.id.toString() == id }
        if (rawTx != null) {
            EditTransactionDialog(
                transaction = rawTx,
                onConfirm = { amt, catId, desc ->
                    viewModel.requestTransactionEdit(id, amt, catId, desc)
                    editingTransactionId = null
                },
                onDismiss = { editingTransactionId = null }
            )
        } else {
            editingTransactionId = null
        }
    }

    // Audit Friction Dialog
    if (uiState.showAuditFrictionDialog) {
        AuditReasonDialog(
            onConfirm = { reason -> viewModel.confirmAuditAction(reason) },
            onDismiss = { viewModel.dismissAuditFrictionDialog() }
        )
    }

    // Audit Logs Visualization
    if (showAuditLogsSheet && uiState.auditLogs.isNotEmpty()) {
        AuditLogBottomSheet(
            auditLogs = uiState.auditLogs,
            onDismiss = { showAuditLogsSheet = false }
        )
    }
}
