package filipe.guerreiro.domain.usecase

import filipe.guerreiro.domain.model.backup.CloudBackup
import filipe.guerreiro.domain.service.backup.DatabaseFileManager
import filipe.guerreiro.domain.service.backup.GoogleDriveClient
import filipe.guerreiro.domain.service.oauth.GoogleAuthService
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

/**
 * Executa o fluxo completo de backup na nuvem:
 * 1. Obtém token de acesso OAuth
 * 2. Exporta o banco de dados local (com close/reopen seguro)
 * 3. Faz upload para o Google Drive appDataFolder
 * 4. Rotaciona backups antigos (mantém apenas os últimos N)
 *
 * SEGURANÇA: O exportDatabase() do DatabaseFileManager já garante
 * que o banco é fechado antes da leitura e reaberto após.
 * Em nenhum momento os dados locais são modificados ou removidos.
 */
class PerformCloudBackupUseCase(
    private val googleAuthService: GoogleAuthService,
    private val databaseFileManager: DatabaseFileManager,
    private val googleDriveClient: GoogleDriveClient
) {
    /**
     * @return CloudBackup com os metadados do backup criado
     * @throws IllegalStateException se não houver sessão autenticada
     * @throws filipe.guerreiro.domain.service.backup.DriveApiException em falha de comunicação
     */
    suspend operator fun invoke(): CloudBackup {
        val accessToken = googleAuthService.getAccessToken()
            ?: throw IllegalStateException("Usuário não autenticado. Faça login antes de realizar o backup.")

        // Exporta bytes do banco SEM modificar o banco local
        val databaseBytes = databaseFileManager.exportDatabase()

        // Gera nome canônico com timestamp
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val fileName = "backup_${now.year}_" +
                "${now.month.number.toString().padStart(2, '0')}_" +
                "${now.day.toString().padStart(2, '0')}_" +
                "${now.hour.toString().padStart(2, '0')}" +
                "${now.minute.toString().padStart(2, '0')}" +
                "${now.second.toString().padStart(2, '0')}.db"

        // Upload para o Google Drive
        val backup = googleDriveClient.uploadBackup(accessToken, fileName, databaseBytes)

        // Rotaciona backups antigos (best-effort, não falha o upload)
        try {
            googleDriveClient.rotateBackups(accessToken)
        } catch (_: Exception) {
            // Rotação é secundária - o backup principal já foi salvo com sucesso
        }

        return backup
    }
}
