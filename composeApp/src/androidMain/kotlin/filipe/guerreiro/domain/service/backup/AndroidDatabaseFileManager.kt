package filipe.guerreiro.domain.service.backup

import android.content.Context
import filipe.guerreiro.data.local.AppDatabase
import filipe.guerreiro.di.databaseDependenciesModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import java.io.File

class AndroidDatabaseFileManager(
    private val context: Context
) : DatabaseFileManager, KoinComponent {

    override suspend fun exportDatabase(): ByteArray = withContext(Dispatchers.IO) {
        val database = getKoin().get<AppDatabase>()
        database.close()

        try {
            context.getDatabasePath(DB_NAME).readBytes()
        } finally {
            reopenDatabase()
        }
    }

    override suspend fun importDatabase(data: ByteArray): Unit = withContext(Dispatchers.IO) {
        val database = getKoin().get<AppDatabase>()
        database.close()

        val dbFile = context.getDatabasePath(DB_NAME)
        val backupFile = File(dbFile.parentFile, "$DB_NAME.safety-backup")

        try {
            // Backup de segurança dos dados atuais
            createSafetyBackup(dbFile, backupFile)

            // Escrever novo banco
            dbFile.writeBytes(data)

            // Limpar arquivos WAL/SHM do banco anterior (evita corrupção)
            clearWalFiles(dbFile)

            // Reabrir para validar integridade
            reopenDatabase()

            // Sucesso confirmado - remover backup de segurança
            cleanupBackupFiles(backupFile)
        } catch (e: Exception) {
            // CRÍTICO: Restaurar dados originais do usuário
            restoreFromBackup(dbFile, backupFile)
            try { reopenDatabase() } catch (_: Exception) {}
            throw DatabaseImportException(
                "Falha ao importar banco de dados. Dados originais foram preservados.",
                e
            )
        }
    }

    private fun createSafetyBackup(dbFile: File, backupFile: File) {
        if (!dbFile.exists()) return
        dbFile.copyTo(backupFile, overwrite = true)
        WAL_SUFFIXES.forEach { suffix ->
            val auxFile = File(dbFile.path + suffix)
            if (auxFile.exists()) {
                auxFile.copyTo(File(backupFile.path + suffix), overwrite = true)
            }
        }
    }

    private fun restoreFromBackup(dbFile: File, backupFile: File) {
        if (!backupFile.exists()) return

        // Limpar arquivos do import falho
        clearWalFiles(dbFile)
        if (dbFile.exists()) dbFile.delete()

        // Restaurar backup
        backupFile.copyTo(dbFile, overwrite = true)
        WAL_SUFFIXES.forEach { suffix ->
            val auxBackup = File(backupFile.path + suffix)
            if (auxBackup.exists()) {
                auxBackup.copyTo(File(dbFile.path + suffix), overwrite = true)
            }
        }
        cleanupBackupFiles(backupFile)
    }

    private fun clearWalFiles(dbFile: File) {
        WAL_SUFFIXES.forEach { suffix ->
            File(dbFile.path + suffix).let { if (it.exists()) it.delete() }
        }
    }

    private fun cleanupBackupFiles(backupFile: File) {
        backupFile.delete()
        WAL_SUFFIXES.forEach { suffix ->
            File(backupFile.path + suffix).let { if (it.exists()) it.delete() }
        }
    }

    /**
     * Reinjecta todas as dependências do banco no Koin.
     * O Builder é um factory, então será recriado automaticamente ao recarregar.
     */
    private fun reopenDatabase() {
        val koin = getKoin()
        koin.unloadModules(listOf(databaseDependenciesModule))
        koin.loadModules(listOf(databaseDependenciesModule))
    }

    companion object {
        internal const val DB_NAME = "caixa.db"
        private val WAL_SUFFIXES = listOf("-wal", "-shm")
    }
}
