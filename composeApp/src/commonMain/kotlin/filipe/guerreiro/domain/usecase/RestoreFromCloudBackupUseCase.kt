package filipe.guerreiro.domain.usecase

import filipe.guerreiro.domain.service.backup.DatabaseFileManager
import filipe.guerreiro.domain.service.backup.DatabaseImportException
import filipe.guerreiro.domain.service.backup.GoogleDriveClient
import filipe.guerreiro.domain.service.oauth.GoogleAuthService

/**
 * Restaura o banco de dados local a partir de um backup na nuvem.
 *
 * SEQUÊNCIA SEGURA:
 * 1. Valida sessão autenticada
 * 2. Faz download dos bytes do Drive
 * 3. Valida integridade do arquivo (magic bytes SQLite)
 * 4. Delega ao DatabaseFileManager que:
 *    a) Cria backup de segurança dos dados atuais
 *    b) Substitui o banco
 *    c) Em caso de falha, restaura o backup automaticamente
 *    d) Reinjecta dependências no Koin
 *
 * GARANTIA DE SEGURANÇA: Se qualquer etapa falhar após o download,
 * o DatabaseFileManager restaura o banco original automaticamente.
 * Os dados do usuário NUNCA são perdidos.
 */
class RestoreFromCloudBackupUseCase(
    private val googleAuthService: GoogleAuthService,
    private val databaseFileManager: DatabaseFileManager,
    private val googleDriveClient: GoogleDriveClient
) {
    companion object {
        // Magic bytes do formato SQLite: "SQLite format 3\0"
        private val SQLITE_MAGIC = "SQLite format 3\u0000".encodeToByteArray()
    }

    /**
     * @param fileId ID do arquivo no Google Drive a ser restaurado
     * @throws IllegalStateException se não houver sessão autenticada
     * @throws DatabaseImportException se a validação ou o import falhar (dados originais preservados)
     * @throws filipe.guerreiro.domain.service.backup.DriveApiException em falha de comunicação
     */
    suspend operator fun invoke(fileId: String) {
        val accessToken = googleAuthService.getAccessToken()
            ?: throw IllegalStateException("Usuário não autenticado. Faça login antes de restaurar o backup.")

        // Download do backup remoto
        val backupBytes = googleDriveClient.downloadBackup(accessToken, fileId)

        // Validação de integridade: verifica se o arquivo é um banco SQLite válido
        if (backupBytes.size < SQLITE_MAGIC.size) {
            throw DatabaseImportException(
                "Arquivo de backup inválido: tamanho insuficiente (${backupBytes.size} bytes)"
            )
        }

        val header = backupBytes.copyOfRange(0, SQLITE_MAGIC.size)
        if (!header.contentEquals(SQLITE_MAGIC)) {
            throw DatabaseImportException(
                "Arquivo de backup corrompido: cabeçalho SQLite inválido"
            )
        }

        // Importa o banco com segurança (backup automático dos dados atuais + rollback em falha)
        databaseFileManager.importDatabase(backupBytes)
    }
}
