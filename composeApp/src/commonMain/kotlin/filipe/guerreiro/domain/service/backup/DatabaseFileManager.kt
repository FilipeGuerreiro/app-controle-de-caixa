package filipe.guerreiro.domain.service.backup

/**
 * Gerencia o arquivo físico do SQLite para operações de backup/restore em nuvem.
 * Implementações específicas por plataforma lidam com o I/O real do arquivo.
 *
 * SEGURANÇA: Todas as operações garantem que o banco é fechado antes da manipulação
 * do arquivo e reaberto após. Em caso de falha no import, os dados originais são restaurados.
 */
interface DatabaseFileManager {

    /**
     * Exporta o banco de dados atual como um array de bytes.
     * Fecha o banco de forma segura, lê o arquivo, e reabre.
     *
     * @return ByteArray contendo o arquivo .db completo
     */
    suspend fun exportDatabase(): ByteArray

    /**
     * Substitui o banco de dados atual pelo array de bytes fornecido.
     * Cria um backup de segurança antes de substituir.
     * Em caso de falha, restaura o backup original automaticamente.
     *
     * Após o import bem-sucedido, todos os componentes dependentes do banco
     * (DAOs, Repositories, SessionManager) são reinjetados no Koin.
     * A camada de UI deve forçar navegação para a tela raiz após esta operação.
     *
     * @param data ByteArray contendo o novo arquivo .db
     * @throws DatabaseImportException se o import falhar (dados originais preservados)
     */
    suspend fun importDatabase(data: ByteArray)
}

class DatabaseImportException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
