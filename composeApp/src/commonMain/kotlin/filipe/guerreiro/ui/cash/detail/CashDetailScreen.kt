package filipe.guerreiro.ui.cash.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import filipe.guerreiro.ui.start.LoadingContent
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun CashDetailScreen(
    cashId: Long,
    onBackClick: () -> Unit,
    onNavigateToTransaction: () -> Unit
) {

    val viewModel: CashDetailViewModel = koinViewModel { parametersOf(cashId) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            CashDetailTopBar(
                title = "Detalhes do Caixa",
                onBackClick = onBackClick
            )
        },
        floatingActionButton = {
            if (uiState.summary.status == "Aberto") {
                FloatingActionButton(
                    onClick = onNavigateToTransaction,
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                ) {
                    Icon(Icons.Default.Add, "Novo Lançamento")
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            LoadingContent() // Seu componente de skeleton ou progresso
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Reaproveitamos seus componentes visuais da CashScreen original
                DailySummaryCard(uiState.summary)
                HistorySection(
                    items = uiState.historyItems,
                    isOpen = uiState.summary.status == "Aberto",
                    onAddTransaction = onNavigateToTransaction
                )
            }
        }
    }
}