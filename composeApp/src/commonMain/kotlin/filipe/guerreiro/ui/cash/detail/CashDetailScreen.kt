package filipe.guerreiro.ui.cash.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
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
                                onManagePaymentMethods = onNavigateToPaymentMethods
                            )
                        }
                        DetailSection.HISTORY -> {
                            HistorySection(
                                items = uiState.historyItems,
                                isOpen = uiState.summary.status == "Aberto",
                                onAddTransaction = onNavigateToTransaction
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
                            ReportsSection()
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
}
