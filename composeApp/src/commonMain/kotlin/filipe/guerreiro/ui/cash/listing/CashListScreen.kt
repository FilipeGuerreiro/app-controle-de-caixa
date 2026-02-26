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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Meus Caixas",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
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
                    state.sessions.isEmpty() -> {
                        EmptySessionsState(modifier = Modifier.fillMaxSize())
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            item {
                                var showDatePicker by remember { mutableStateOf(false) }

                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showDatePicker = true },
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        MaterialTheme.colorScheme.outlineVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = "Filtrar por data",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = if (state.selectedDateMillis != null) {
                                                val millis = state.selectedDateMillis
                                                val days = millis / 86400000L
                                                val day = ((days % 30.44) + 1).toInt()
                                                val month = (((days % 365.25) / 30.44) + 1).toInt().coerceIn(1, 12)
                                                val year = (1970 + (days / 365.25)).toInt()
                                                val dayFormatted = day.toString().padStart(2, '0')
                                                val monthFormatted = month.toString().padStart(2, '0')
                                                val yearFormatted = year.toString().padStart(4, '0')
                                                "$dayFormatted/$monthFormatted/$yearFormatted"
                                            } else {
                                                "Filtrar por data..."
                                            },
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = if (state.selectedDateMillis != null)
                                                MaterialTheme.colorScheme.onSurface
                                            else
                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.weight(1f)
                                        )
                                        if (state.selectedDateMillis != null) {
                                            IconButton(
                                                onClick = { viewModel.updateSelectedDate(null) }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = "Limpar data",
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                if (showDatePicker) {
                                    val datePickerState = rememberDatePickerState(
                                        initialSelectedDateMillis = state.selectedDateMillis
                                    )
                                    DatePickerDialog(
                                        onDismissRequest = { showDatePicker = false },
                                        confirmButton = {
                                            TextButton(
                                                onClick = {
                                                    viewModel.updateSelectedDate(datePickerState.selectedDateMillis)
                                                    showDatePicker = false
                                                }
                                            ) {
                                                Text("Confirmar")
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showDatePicker = false }) {
                                                Text("Cancelar")
                                            }
                                        }
                                    ) {
                                        DatePicker(state = datePickerState)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                CashListFilterBar(
                                    selectedFilter = state.activeFilterTab,
                                    onFilterSelected = viewModel::updateActiveFilterTab,
                                    onClearFilter = viewModel::clearFilters,
                                    modifier = Modifier.padding(horizontal = 0.dp)
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                            }

                            if (state.currentSession != null) {
                                item {
                                    Text(
                                        text = "Caixa Atual",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )
                                    CashSessionProminentCard(
                                        session = state.currentSession,
                                        onClick = { onSessionClick(state.currentSession.id) }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }

                            if (state.groupedSessions.isEmpty()) {
                                item {
                                    EmptySessionsState(modifier = Modifier.fillMaxSize())
                                }
                            } else {
                                state.groupedSessions.forEach { (monthLabel, sessions) ->
                                    item {
                                        Row(
                                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = monthLabel,
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }

                                    itemsIndexed(
                                        items = sessions,
                                        key = { _, item -> item.id }
                                    ) { index, session ->
                                        CashSessionListItem(
                                            session = session,
                                            onClick = { onSessionClick(session.id) },
                                            modifier = Modifier.padding(bottom = if (index < sessions.size - 1) 8.dp else 0.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
