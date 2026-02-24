package filipe.guerreiro.ui.cash.listing

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashListScreen(
    onSessionClick: (Long) -> Unit,
    viewModel: CashListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Meus Caixas",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    IconButton(onClick = { 
                        if (uiState.filters.isActive) viewModel.clearFilters() else showFilterSheet = true 
                    }) {
                        Icon(
                            imageVector = if (uiState.filters.isActive) Icons.Default.FilterListOff else Icons.Default.FilterList,
                            contentDescription = "Filtrar",
                            tint = if (uiState.filters.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { state ->
                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    state.error != null -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = state.error,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    state.filteredSessions.isEmpty() -> {
                        EmptySessionsState(modifier = Modifier.fillMaxSize())
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {

                            val firstSession = state.filteredSessions.first()
                            item {
                                Text(
                                    text = "Caixa Atual",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                CashSessionProminentCard(
                                    session = firstSession,
                                    onClick = { onSessionClick(firstSession.id) }
                                )
                            }

                            if (state.filteredSessions.size > 1) {
                                item {
                                    Row(
                                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Histórico",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        if (state.filters.isActive) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "(Filtrado)",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                itemsIndexed(
                                    items = state.filteredSessions.drop(1),
                                    key = { _, item -> item.id }
                                ) { index, session ->
                                    CashSessionListItem(
                                        session = session,
                                        onClick = { onSessionClick(session.id) },
                                        modifier = Modifier.padding(bottom = if (index < state.filteredSessions.size - 2) 8.dp else 0.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showFilterSheet) {
            FilterBottomSheet(
                currentFilters = uiState.filters,
                onApply = { 
                    viewModel.updateFilters(it)
                    showFilterSheet = false 
                },
                onDismiss = { showFilterSheet = false },
                onClear = { 
                    viewModel.clearFilters()
                    showFilterSheet = false
                }
            )
        }
    }
}
