package filipe.guerreiro.ui.cash.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
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
import filipe.guerreiro.ui.components.ExpandableFab
import filipe.guerreiro.ui.components.FabItem
import filipe.guerreiro.ui.components.FabScrim
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashDetailScreen(
    cashId: Long,
    onBackClick: () -> Unit,
    onNavigateToTransaction: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToPaymentMethods: () -> Unit
) {

    val viewModel: CashDetailViewModel = koinViewModel { parametersOf(cashId) }
    val uiState by viewModel.uiState.collectAsState()
    var isFabExpanded by remember { mutableStateOf(false) }
    var isMinLoadingElapsed by remember { mutableStateOf(false) }

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
        },
        floatingActionButton = {
            if (uiState.summary.status == "Aberto") {
                ExpandableFab(
                    isExpanded = isFabExpanded,
                    onToggle = { isFabExpanded = !isFabExpanded },
                    items = listOf(
                        FabItem(
                            icon = Icons.Default.Add,
                            label = "Novo Lançamento",
                            onClick = onNavigateToTransaction
                        ),
                        FabItem(
                            icon = Icons.Default.Category,
                            label = "Categorias",
                            onClick = onNavigateToCategories
                        ),
                        FabItem(
                            icon = Icons.Default.CreditCard,
                            label = "Métodos de Pagamento",
                            onClick = onNavigateToPaymentMethods
                        ),
//                        FabItem(
//                            icon = Icons.Default.Assessment,
//                            label = "Gerar Relatório",
//                            onClick = {
//                                // Placeholder for now
//                            }
//                        )
                    )
                )
            }
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
                    DailySummaryCard(uiState.summary)
                    HistorySection(
                        items = uiState.historyItems,
                        isOpen = uiState.summary.status == "Aberto",
                        onAddTransaction = onNavigateToTransaction
                    )
                }
            }

            // Scrim overlay to close FAB when clicking outside
            if (isFabExpanded) {
                FabScrim(
                    isExpanded = isFabExpanded,
                    onClose = { isFabExpanded = false }
                )
            }
        }
    }
}
