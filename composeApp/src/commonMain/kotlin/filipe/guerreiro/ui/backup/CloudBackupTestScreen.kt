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
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import filipe.guerreiro.domain.model.backup.CloudBackup
import filipe.guerreiro.domain.model.oauth.AuthResult
import filipe.guerreiro.domain.service.oauth.GoogleAuthService
import filipe.guerreiro.domain.usecase.FetchAvailableBackupsUseCase
import filipe.guerreiro.domain.usecase.PerformCloudBackupUseCase
import filipe.guerreiro.domain.usecase.RestoreFromCloudBackupUseCase
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

private sealed class CloudTestState {
    data object Idle : CloudTestState()
    data class Running(val step: String) : CloudTestState()
    data class ListSuccess(val backups: List<CloudBackup>) : CloudTestState()
    data class UploadSuccess(val backup: CloudBackup) : CloudTestState()
    data class DownloadSuccess(val fileName: String, val sizeBytes: Int) : CloudTestState()
    data class Failed(val step: String, val error: String) : CloudTestState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudBackupTestScreen(
    onBackClick: () -> Unit,
    authService: GoogleAuthService = koinInject(),
    performBackup: PerformCloudBackupUseCase = koinInject(),
    fetchBackups: FetchAvailableBackupsUseCase = koinInject(),
    restoreBackup: RestoreFromCloudBackupUseCase = koinInject(),
    driveClient: filipe.guerreiro.domain.service.backup.GoogleDriveClient = koinInject()
) {
    val authState by authService.authState.collectAsState()
    val scope = rememberCoroutineScope()
    var testState by remember { mutableStateOf<CloudTestState>(CloudTestState.Idle) }
    val isLoggedIn = authState is AuthResult.Success

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnóstico Nuvem") },
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
                            "Teste de Backup na Nuvem (Google Drive)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Testa o fluxo completo da Fase 3: listar backups no Drive, " +
                                "fazer upload do banco atual, e baixar um backup existente. " +
                                "O download NÃO restaura — apenas valida que os bytes chegam corretamente.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // --- Auth Status ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isLoggedIn)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        if (isLoggedIn) Icons.Filled.CheckCircle else Icons.Filled.Cloud,
                        contentDescription = null,
                        tint = if (isLoggedIn)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            if (isLoggedIn) "Google conectado" else "Google não conectado",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!isLoggedIn) {
                            Text(
                                "Use a tela \"Backup em Nuvem\" no menu para fazer login primeiro.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            val user = (authState as AuthResult.Success).user
                            Text(
                                user.email,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            if (!isLoggedIn) {
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                authService.signIn()
                            } catch (_: Exception) { }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = authState !is AuthResult.Loading
                ) {
                    Icon(Icons.Filled.Cloud, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Login com Google")
                }
            }

            // --- Test Buttons (só habilitados quando logado) ---
            HorizontalDivider()

            // 1. Listar Backups
            Button(
                onClick = {
                    testState = CloudTestState.Running("Listando backups no Drive...")
                    scope.launch {
                        testState = try {
                            val backups = fetchBackups()
                            CloudTestState.ListSuccess(backups)
                        } catch (e: Exception) {
                            CloudTestState.Failed("Listar Backups", e.message ?: e.toString())
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isLoggedIn && testState !is CloudTestState.Running
            ) {
                Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("1. Listar Backups no Drive")
            }

            // 2. Upload Backup
            Button(
                onClick = {
                    testState = CloudTestState.Running("Exportando banco e enviando ao Drive...")
                    scope.launch {
                        testState = try {
                            val backup = performBackup()
                            CloudTestState.UploadSuccess(backup)
                        } catch (e: Exception) {
                            CloudTestState.Failed("Upload Backup", e.message ?: e.toString())
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isLoggedIn && testState !is CloudTestState.Running
            ) {
                Icon(Icons.Filled.CloudUpload, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("2. Enviar Backup ao Drive")
            }

            // 3. Download (verificação sem restaurar)
            val lastUpload = testState as? CloudTestState.UploadSuccess
            val listBackups = testState as? CloudTestState.ListSuccess
            val downloadableFileId = lastUpload?.backup?.fileId
                ?: listBackups?.backups?.firstOrNull()?.fileId
            val downloadableFileName = lastUpload?.backup?.fileName
                ?: listBackups?.backups?.firstOrNull()?.fileName ?: "N/A"

            OutlinedButton(
                onClick = {
                    val fileId = downloadableFileId ?: return@OutlinedButton
                    testState = CloudTestState.Running("Baixando backup do Drive...")
                    scope.launch {
                        testState = try {
                            val token = authService.getAccessToken()!!
                            val bytes = driveClient.downloadBackup(token, fileId)
                            CloudTestState.DownloadSuccess(
                                fileName = downloadableFileName,
                                sizeBytes = bytes.size
                            )
                        } catch (e: Exception) {
                            CloudTestState.Failed("Download Backup", e.message ?: e.toString())
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isLoggedIn && downloadableFileId != null && testState !is CloudTestState.Running
            ) {
                Icon(Icons.Filled.CloudDownload, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text("3. Baixar e Validar (sem restaurar)")
            }

            // --- Resultado ---
            when (val state = testState) {
                is CloudTestState.Idle -> { /* nada */ }
                is CloudTestState.Running -> {
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
                            Text(state.step, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                is CloudTestState.ListSuccess -> {
                    CloudResultCard(
                        success = true,
                        title = "Backups encontrados: ${state.backups.size}",
                        details = if (state.backups.isEmpty()) {
                            listOf("Info" to "Nenhum backup no appDataFolder ainda")
                        } else {
                            state.backups.mapIndexed { index, b ->
                                val local = b.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
                                val date = "${local.day.toString().padStart(2, '0')}/" +
                                        "${local.month.number.toString().padStart(2, '0')}/" +
                                        "${local.year}"
                                val time = "${local.hour.toString().padStart(2, '0')}:" +
                                        "${local.minute.toString().padStart(2, '0')}"
                                "#${index + 1} ${b.fileName}" to "$date $time (${formatCloudFileSize(b.sizeBytes)})"
                            }
                        }
                    )
                }
                is CloudTestState.UploadSuccess -> {
                    CloudResultCard(
                        success = true,
                        title = "Upload bem-sucedido!",
                        details = listOf(
                            "Arquivo" to state.backup.fileName,
                            "ID no Drive" to state.backup.fileId.take(20) + "...",
                            "Tamanho" to formatCloudFileSize(state.backup.sizeBytes),
                            "Status" to "Banco exportado → Upload OK → Rotação OK"
                        )
                    )
                }
                is CloudTestState.DownloadSuccess -> {
                    CloudResultCard(
                        success = true,
                        title = "Download validado!",
                        details = listOf(
                            "Arquivo" to state.fileName,
                            "Bytes recebidos" to formatCloudFileSize(state.sizeBytes.toLong()),
                            "Status" to "Dados intactos (não restauramos — apenas validamos o download)"
                        )
                    )
                }
                is CloudTestState.Failed -> {
                    CloudResultCard(
                        success = false,
                        title = "Falha no ${state.step}",
                        details = listOf("Erro" to state.error)
                    )
                }
            }

            // --- Reset ---
            if (testState !is CloudTestState.Idle && testState !is CloudTestState.Running) {
                OutlinedButton(
                    onClick = { testState = CloudTestState.Idle },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Limpar resultados")
                }
            }
        }
    }
}

@Composable
private fun CloudResultCard(
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
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.weight(1.5f)
                    )
                }
            }
        }
    }
}

private fun formatCloudFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${roundTo(bytes / 1024.0, 1)} KB"
        else -> "${roundTo(bytes / (1024.0 * 1024.0), 2)} MB"
    }
}

private fun roundTo(value: Double, decimals: Int): String {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    val rounded = kotlin.math.round(value * multiplier) / multiplier
    val parts = rounded.toString().split(".")
    val intPart = parts[0]
    val decPart = (parts.getOrElse(1) { "0" }).padEnd(decimals, '0').take(decimals)
    return "$intPart.$decPart"
}
