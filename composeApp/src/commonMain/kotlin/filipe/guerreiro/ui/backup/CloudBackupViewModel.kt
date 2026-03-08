package filipe.guerreiro.ui.backup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import filipe.guerreiro.domain.model.backup.CloudBackup
import filipe.guerreiro.domain.model.oauth.AuthResult
import filipe.guerreiro.domain.model.oauth.GoogleUser
import filipe.guerreiro.domain.service.oauth.GoogleAuthService
import filipe.guerreiro.domain.usecase.FetchAvailableBackupsUseCase
import filipe.guerreiro.domain.usecase.PerformCloudBackupUseCase
import filipe.guerreiro.domain.usecase.RestoreFromCloudBackupUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Estados da tela de Backup em Nuvem.
 *
 * A tela possui um fluxo linear:
 * Disconnected → Connecting (silently) → Connected (com lista) → BlockingOperation (durante backup/restore)
 *
 * BlockingOperation impede TODA interação (back, cliques, gestos) para proteger
 * a integridade do banco de dados durante operações de I/O.
 */
sealed class CloudBackupUiState {
    /** Usuário não autenticado. Mostra botão de login. */
    data object Disconnected : CloudBackupUiState()

    /** Tentando restaurar sessão anterior (signInSilently). */
    data object Connecting : CloudBackupUiState()

    /** Autenticado. Exibe lista de backups e ações disponíveis. */
    data class Connected(
        val user: GoogleUser,
        val backups: List<CloudBackup> = emptyList(),
        val isLoadingBackups: Boolean = false,
    ) : CloudBackupUiState()

    /**
     * Operação bloqueante em andamento.
     * UI deve exibir overlay full-screen que impede back navigation e cliques.
     */
    data class BlockingOperation(
        val message: String,
    ) : CloudBackupUiState()
}

/**
 * Eventos pontuais (one-shot) emitidos pelo ViewModel para a UI reagir.
 * Separados do estado para evitar re-exibição em recomposições.
 */
sealed class CloudBackupEvent {
    data class BackupSuccess(val backup: CloudBackup) : CloudBackupEvent()
    data object RestoreSuccess : CloudBackupEvent()
    data class Error(val message: String) : CloudBackupEvent()
}

class CloudBackupViewModel(
    private val googleAuthService: GoogleAuthService,
    private val performCloudBackupUseCase: PerformCloudBackupUseCase,
    private val fetchAvailableBackupsUseCase: FetchAvailableBackupsUseCase,
    private val restoreFromCloudBackupUseCase: RestoreFromCloudBackupUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<CloudBackupUiState>(CloudBackupUiState.Disconnected)
    val uiState: StateFlow<CloudBackupUiState> = _uiState.asStateFlow()

    private val _event = MutableStateFlow<CloudBackupEvent?>(null)
    val event: StateFlow<CloudBackupEvent?> = _event.asStateFlow()

    init {
        trySilentSignIn()
    }

    fun consumeEvent() {
        _event.value = null
    }

    // ── Autenticação ──────────────────────────────────────────────────

    private fun trySilentSignIn() {
        viewModelScope.launch {
            _uiState.value = CloudBackupUiState.Connecting
            val result = googleAuthService.signInSilently()
            handleAuthResult(result)
        }
    }

    fun signIn() {
        viewModelScope.launch {
            _uiState.value = CloudBackupUiState.Connecting
            val result = googleAuthService.signIn()
            handleAuthResult(result)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            googleAuthService.signOut()
            _uiState.value = CloudBackupUiState.Disconnected
        }
    }

    private fun handleAuthResult(result: AuthResult) {
        when (result) {
            is AuthResult.Success -> {
                _uiState.value = CloudBackupUiState.Connected(
                    user = result.user,
                    isLoadingBackups = true,
                )
                loadBackups(result.user)
            }
            is AuthResult.Error -> {
                _uiState.value = CloudBackupUiState.Disconnected
                _event.value = CloudBackupEvent.Error(
                    result.message
                )
            }
            is AuthResult.Cancelled -> {
                _uiState.value = CloudBackupUiState.Disconnected
            }
            is AuthResult.SignedOut, is AuthResult.Loading -> {
                _uiState.value = CloudBackupUiState.Disconnected
            }
        }
    }

    // ── Operações de Backup ───────────────────────────────────────────

    fun refreshBackups() {
        val current = _uiState.value
        if (current !is CloudBackupUiState.Connected) return

        _uiState.value = current.copy(isLoadingBackups = true)
        loadBackups(current.user)
    }

    private fun loadBackups(user: GoogleUser) {
        viewModelScope.launch {
            try {
                val backups = fetchAvailableBackupsUseCase()
                _uiState.value = CloudBackupUiState.Connected(
                    user = user,
                    backups = backups,
                    isLoadingBackups = false,
                )
            } catch (e: Exception) {
                _uiState.value = CloudBackupUiState.Connected(
                    user = user,
                    backups = emptyList(),
                    isLoadingBackups = false,
                )
                _event.value = CloudBackupEvent.Error(
                    "Falha ao carregar backups: ${e.message}"
                )
            }
        }
    }

    /**
     * Realiza backup completo.
     * BLOQUEANTE: impede toda interação na UI até conclusão.
     */
    fun performBackup() {
        val current = _uiState.value
        if (current !is CloudBackupUiState.Connected) return
        val user = current.user

        viewModelScope.launch {
            _uiState.value = CloudBackupUiState.BlockingOperation(
                message = "Realizando backup…\nNão feche o aplicativo."
            )
            try {
                val backup = performCloudBackupUseCase()
                _event.value = CloudBackupEvent.BackupSuccess(backup)
                // Recarrega lista após sucesso
                _uiState.value = CloudBackupUiState.Connected(
                    user = user,
                    isLoadingBackups = true,
                )
                loadBackups(user)
            } catch (e: Exception) {
                _event.value = CloudBackupEvent.Error(
                    "Falha ao realizar backup: ${e.message}"
                )
                _uiState.value = CloudBackupUiState.Connected(user = user)
                loadBackups(user)
            }
        }
    }

    /**
     * Restaura banco a partir de um backup na nuvem.
     * BLOQUEANTE e CRÍTICO: fecha o banco atual, substitui, e reinjecta Koin.
     * A UI DEVE forçar navegação para a tela raiz após o sucesso.
     */
    fun restoreBackup(fileId: String) {
        val current = _uiState.value
        if (current !is CloudBackupUiState.Connected) return
        val user = current.user

        viewModelScope.launch {
            _uiState.value = CloudBackupUiState.BlockingOperation(
                message = "Restaurando dados…\nNão feche o aplicativo.\nIsso pode levar alguns segundos."
            )
            try {
                restoreFromCloudBackupUseCase(fileId)
                _event.value = CloudBackupEvent.RestoreSuccess
            } catch (e: Exception) {
                _event.value = CloudBackupEvent.Error(
                    "Falha ao restaurar backup: ${e.message}"
                )
                _uiState.value = CloudBackupUiState.Connected(user = user)
                loadBackups(user)
            }
        }
    }
}
