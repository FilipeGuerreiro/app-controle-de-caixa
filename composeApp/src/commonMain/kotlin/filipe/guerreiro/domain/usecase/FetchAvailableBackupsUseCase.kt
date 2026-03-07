package filipe.guerreiro.domain.usecase

import filipe.guerreiro.domain.model.backup.CloudBackup
import filipe.guerreiro.domain.service.backup.GoogleDriveClient
import filipe.guerreiro.domain.service.oauth.GoogleAuthService

/**
 * Busca a lista de backups disponíveis no Google Drive appDataFolder.
 * Retorna a lista ordenada do mais recente ao mais antigo.
 *
 * SEGURANÇA: Operação puramente de leitura (GET). Não modifica
 * nenhum dado local ou remoto.
 */
class FetchAvailableBackupsUseCase(
    private val googleAuthService: GoogleAuthService,
    private val googleDriveClient: GoogleDriveClient
) {
    /**
     * @return Lista de CloudBackup ordenada por data de criação (desc)
     * @throws IllegalStateException se não houver sessão autenticada
     * @throws filipe.guerreiro.domain.service.backup.DriveApiException em falha de comunicação
     */
    suspend operator fun invoke(): List<CloudBackup> {
        val accessToken = googleAuthService.getAccessToken()
            ?: throw IllegalStateException("Usuário não autenticado. Faça login antes de consultar os backups.")

        return googleDriveClient.listBackups(accessToken)
    }
}
