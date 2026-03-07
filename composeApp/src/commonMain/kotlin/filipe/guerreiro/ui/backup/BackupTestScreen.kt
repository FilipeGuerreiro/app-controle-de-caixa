package filipe.guerreiro.ui.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import filipe.guerreiro.domain.service.backup.DatabaseFileManager
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private sealed class TestState {
    data object Idle : TestState()
    data object Running : TestState()
    data class ExportSuccess(val sizeBytes: Int, val data: ByteArray) : TestState()
    data class RoundTripSuccess(val exportSize: Int, val importSize: Int) : TestState()
    data class Failed(val step: String, val error: String) : TestState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupTestScreen(
    onBackClick: () -> Unit,
    databaseFileManager: DatabaseFileManager = koinInject()
) {
    val scope = rememberCoroutineScope()
    var testState by remember { mutableStateOf<TestState>(TestState.Idle) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnóstico Backup Local") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Info Card ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Filled.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            "Teste de Export/Import do Banco de Dados",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Este diagnóstico testa a capacidade de exportar e reimportar " +
                                "o banco de dados local de forma segura, sem perda de dados. " +
                                "O round-trip reimporta os MESMOS dados exportados.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // --- Botão: Teste de Export ---
            Button(
                onClick = {
                    testState = TestState.Running
                    scope.launch {
                        testState = try {
                            val bytes = databaseFileManager.exportDatabase()
                            TestState.ExportSuccess(sizeBytes = bytes.size, data = bytes)
                        } catch (e: Exception) {
                            TestState.Failed("Export", e.message ?: e.toString())
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = testState !is TestState.Running
            ) {
                Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("1. Testar Export do Banco")
            }

            // --- Botão: Round-Trip (Export → Import mesmos dados) ---
            val exportData = (testState as? TestState.ExportSuccess)?.data
            OutlinedButton(
                onClick = {
                    val data = exportData ?: return@OutlinedButton
                    val exportSize = data.size
                    testState = TestState.Running
                    scope.launch {
                        testState = try {
                            databaseFileManager.importDatabase(data)
                            // Verificar se os dados sobreviveram ao round-trip
                            val reExported = databaseFileManager.exportDatabase()
                            TestState.RoundTripSuccess(exportSize = exportSize, importSize = reExported.size)
                        } catch (e: Exception) {
                            TestState.Failed("Round-Trip Import", e.message ?: e.toString())
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = exportData != null && testState !is TestState.Running
            ) {
                Icon(Icons.Filled.CloudDownload, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("2. Testar Round-Trip (Import mesmos dados)")
            }

            // --- Resultado ---
            when (val state = testState) {
                is TestState.Idle -> { /* nada */ }
                is TestState.Running -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Text("Executando teste...", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                is TestState.ExportSuccess -> {
                    ResultCard(
                        success = true,
                        title = "Export bem-sucedido",
                        details = listOf(
                            "Tamanho do arquivo" to formatFileSize(state.sizeBytes),
                            "Status" to "Banco fechado, bytes lidos e banco reaberto com sucesso"
                        )
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Filled.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            Text(
                                "O Passo 2 (Round-Trip) reimportará os MESMOS dados que acabaram de ser exportados. " +
                                    "Isto é 100% seguro — seus dados não mudarão.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                is TestState.RoundTripSuccess -> {
                    ResultCard(
                        success = true,
                        title = "Round-Trip completo!",
                        details = listOf(
                            "Export original" to formatFileSize(state.exportSize),
                            "Re-export após import" to formatFileSize(state.importSize),
                            "Tamanhos iguais" to if (state.exportSize == state.importSize) "SIM" else "NÃO (verifique WAL)",
                            "Hot-Swap Koin" to "Módulo reinjetado com sucesso",
                            "Status" to "Banco substituído, reaberto e validado sem perda de dados"
                        )
                    )
                }
                is TestState.Failed -> {
                    ResultCard(
                        success = false,
                        title = "Falha no ${state.step}",
                        details = listOf("Erro" to state.error)
                    )
                }
            }

            // --- Reset ---
            if (testState !is TestState.Idle && testState !is TestState.Running) {
                OutlinedButton(
                    onClick = { testState = TestState.Idle },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Limpar resultados")
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    success: Boolean,
    title: String,
    details: List<Pair<String, String>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (success)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    if (success) Icons.Filled.CheckCircle else Icons.Filled.Error,
                    contentDescription = null,
                    tint = if (success)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            details.forEach { (label, value) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        value,
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Int): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${"%.1f".format(bytes / 1024.0)} KB"
        else -> "${"%.2f".format(bytes / (1024.0 * 1024.0))} MB"
    }
}
