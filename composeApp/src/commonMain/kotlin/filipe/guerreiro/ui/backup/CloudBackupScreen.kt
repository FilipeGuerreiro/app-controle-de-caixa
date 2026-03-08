package filipe.guerreiro.ui.backup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BackupTable
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import filipe.guerreiro.domain.model.backup.CloudBackup
import filipe.guerreiro.domain.model.oauth.GoogleUser
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.number
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudBackupScreen(
    onBackClick: () -> Unit,
    onRestoreSuccess: () -> Unit,
    viewModel: CloudBackupViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val event by viewModel.event.collectAsState()

    // Dialogs
    var showBackupConfirmDialog by remember { mutableStateOf(false) }
    var restoreTarget by remember { mutableStateOf<CloudBackup?>(null) }
    var snackMessage by remember { mutableStateOf<String?>(null) }

    // Consome eventos one-shot
    LaunchedEffect(event) {
        when (val e = event) {
            is CloudBackupEvent.BackupSuccess -> {
                snackMessage = "Backup realizado com sucesso!"
                viewModel.consumeEvent()
            }
            is CloudBackupEvent.RestoreSuccess -> {
                viewModel.consumeEvent()
                onRestoreSuccess()
            }
            is CloudBackupEvent.Error -> {
                snackMessage = e.message
                viewModel.consumeEvent()
            }
            null -> {}
        }
    }

    // Bloqueia interação durante operação bloqueante
    // O overlay full-screen consome todos os toques; o botão de voltar está desabilitado.
    // A operação continua no ViewModelScope mesmo se o usuário sair da tela.

    val isBlocking = uiState is CloudBackupUiState.BlockingOperation

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Backup em Nuvem") },
                    navigationIcon = {
                        IconButton(
                            onClick = onBackClick,
                            enabled = !isBlocking,
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar"
                            )
                        }
                    },
                    actions = {
                        val currentState = uiState
                        if (currentState is CloudBackupUiState.Connected) {
                            IconButton(
                                onClick = { viewModel.refreshBackups() },
                                enabled = !currentState.isLoadingBackups,
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Atualizar")
                            }
                        }
                    }
                )
            }
        ) { padding ->
            when (val state = uiState) {
                is CloudBackupUiState.Disconnected -> {
                    DisconnectedContent(
                        modifier = Modifier.padding(padding),
                        onSignIn = { viewModel.signIn() },
                    )
                }

                is CloudBackupUiState.Connecting -> {
                    ConnectingContent(
                        modifier = Modifier.padding(padding),
                    )
                }

                is CloudBackupUiState.Connected -> {
                    ConnectedContent(
                        modifier = Modifier.padding(padding),
                        user = state.user,
                        backups = state.backups,
                        isLoadingBackups = state.isLoadingBackups,
                        onBackupClick = { showBackupConfirmDialog = true },
                        onRestoreClick = { backup -> restoreTarget = backup },
                        onSignOut = { viewModel.signOut() },
                    )
                }

                is CloudBackupUiState.BlockingOperation -> {
                    // O conteúdo principal fica escurecido; o overlay é desenhado fora do Scaffold
                    ConnectedContent(
                        modifier = Modifier.padding(padding),
                        user = GoogleUser("", "", null, null),
                        backups = emptyList(),
                        isLoadingBackups = true,
                        onBackupClick = {},
                        onRestoreClick = {},
                        onSignOut = {},
                    )
                }
            }
        }

        // ── Overlay Bloqueante ─────────────────────────────────────────
        AnimatedVisibility(
            visible = isBlocking,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            val blockingState = uiState as? CloudBackupUiState.BlockingOperation
            BlockingOverlay(
                message = blockingState?.message ?: "Processando…"
            )
        }

        // ── Snackbar-like message at bottom ────────────────────────────
        AnimatedVisibility(
            visible = snackMessage != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            snackMessage?.let { msg ->
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(3000)
                    snackMessage = null
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.inverseSurface
                    )
                ) {
                    Text(
                        text = msg,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.inverseOnSurface,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }

    // ── Diálogos ───────────────────────────────────────────────────────

    if (showBackupConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showBackupConfirmDialog = false },
            icon = { Icon(Icons.Default.CloudUpload, contentDescription = null) },
            title = { Text("Realizar Backup") },
            text = {
                Text(
                    "O backup enviará uma cópia do seu banco de dados para o Google Drive. " +
                        "Seus dados locais não serão alterados."
                )
            },
            confirmButton = {
                Button(onClick = {
                    showBackupConfirmDialog = false
                    viewModel.performBackup()
                }) {
                    Text("Fazer Backup")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    restoreTarget?.let { backup ->
        AlertDialog(
            onDismissRequest = { restoreTarget = null },
            icon = {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text("Restaurar Backup") },
            text = {
                Column {
                    Text(
                        "ATENÇÃO: Esta operação substituirá TODOS os dados atuais do aplicativo " +
                            "pelos dados deste backup.",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error,
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Arquivo: ${backup.fileName}")
                    Text("Tamanho: ${formatFileSize(backup.sizeBytes)}")
                    Text("Data: ${formatBackupDate(backup)}")
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Um backup de segurança dos dados atuais será criado automaticamente " +
                            "antes da restauração. Em caso de falha, os dados originais serão preservados.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val fileId = backup.fileId
                        restoreTarget = null
                        viewModel.restoreBackup(fileId)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Restaurar")
                }
            },
            dismissButton = {
                TextButton(onClick = { restoreTarget = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  Sub-composables
// ══════════════════════════════════════════════════════════════════════════════

@Composable
private fun DisconnectedContent(
    modifier: Modifier = Modifier,
    onSignIn: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            Icons.Default.Cloud,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "Backup em Nuvem",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Faça login com sua conta Google para manter seus dados seguros na nuvem. " +
                "Seus backups ficam armazenados de forma privada no seu Google Drive.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onSignIn,
            modifier = Modifier.fillMaxWidth(0.8f),
        ) {
            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Entrar com Google")
        }
    }
}

@Composable
private fun ConnectingContent(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text(
                "Conectando…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ConnectedContent(
    modifier: Modifier = Modifier,
    user: GoogleUser,
    backups: List<CloudBackup>,
    isLoadingBackups: Boolean,
    onBackupClick: () -> Unit,
    onRestoreClick: (CloudBackup) -> Unit,
    onSignOut: () -> Unit,
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Espaçamento superior
        item { Spacer(Modifier.height(4.dp)) }

        // Card do usuário logado
        item {
            UserInfoCard(user = user, onSignOut = onSignOut)
        }

        // Botão de backup
        item {
            Button(
                onClick = onBackupClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoadingBackups,
            ) {
                Icon(
                    Icons.Default.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text("Fazer Backup Agora")
            }
        }

        // Título da seção de backups
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    Icons.Default.BackupTable,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Backups Disponíveis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        // Estado de loading
        if (isLoadingBackups) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
        } else if (backups.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            Icons.Default.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Nenhum backup encontrado",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            "Faça seu primeiro backup para manter seus dados seguros.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        } else {
            items(backups, key = { it.fileId }) { backup ->
                BackupListItem(
                    backup = backup,
                    onRestoreClick = { onRestoreClick(backup) },
                )
            }
        }

        // Espaçamento inferior
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun UserInfoCard(
    user: GoogleUser,
    onSignOut: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.displayName ?: "Conta Google",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = onSignOut) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = "Desconectar",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            }
        }
    }
}

@Composable
private fun BackupListItem(
    backup: CloudBackup,
    onRestoreClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                Icons.Default.CloudDownload,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = formatBackupDate(backup),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = formatFileSize(backup.sizeBytes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            OutlinedButton(onClick = onRestoreClick) {
                Text("Restaurar")
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  Overlay Bloqueante (Desafio C)
// ══════════════════════════════════════════════════════════════════════════════

/**
 * Overlay full-screen que impede toda interação do usuário.
 * Cobre toda a tela incluindo top bar e bottom nav.
 * BackHandler já está configurado na tela pai.
 */
@Composable
private fun BlockingOverlay(
    message: String,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f))
            .pointerInput(Unit) {
                // Consome todos os gestos de toque para impedir interação
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                    }
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                Spacer(Modifier.height(20.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
//  Utilitários de formatação
// ══════════════════════════════════════════════════════════════════════════════

private fun formatBackupDate(backup: CloudBackup): String {
    return try {
        val local = backup.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
        "${local.day.toString().padStart(2, '0')}/" +
            "${local.month.number.toString().padStart(2, '0')}/" +
            "${local.year} às " +
            "${local.hour.toString().padStart(2, '0')}:" +
            "${local.minute.toString().padStart(2, '0')}"
    } catch (_: Exception) {
        backup.fileName
    }
}

private fun formatFileSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> {
            val mb = bytes.toDouble() / (1024.0 * 1024.0)
            val whole = mb.toLong()
            val frac = ((mb - whole) * 10).toLong()
            "$whole,$frac MB"
        }
    }
}
