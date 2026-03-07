@file:OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)

package filipe.guerreiro.domain.service.backup

import filipe.guerreiro.data.local.AppDatabase
import filipe.guerreiro.di.databaseDependenciesModule
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import platform.Foundation.NSData
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.Foundation.create
import platform.Foundation.writeToFile
import platform.posix.memcpy

class IosDatabaseFileManager : DatabaseFileManager, KoinComponent {

    override suspend fun exportDatabase(): ByteArray = withContext(Dispatchers.IO) {
        val database = getKoin().get<AppDatabase>()
        database.close()

        try {
            val dbPath = getDbPath()
            readFileBytes(dbPath)
        } finally {
            reopenDatabase()
        }
    }

    override suspend fun importDatabase(data: ByteArray): Unit = withContext(Dispatchers.IO) {
        val database = getKoin().get<AppDatabase>()
        database.close()

        val dbPath = getDbPath()
        val backupPath = "$dbPath.safety-backup"
        val fileManager = NSFileManager.defaultManager

        try {
            // Backup de segurança dos dados atuais
            createSafetyBackup(fileManager, dbPath, backupPath)

            // Escrever novo banco
            writeFileBytes(dbPath, data)

            // Limpar arquivos WAL/SHM do banco anterior
            clearWalFiles(fileManager, dbPath)

            // Reabrir para validar integridade
            reopenDatabase()

            // Sucesso confirmado - remover backup de segurança
            cleanupBackupFiles(fileManager, backupPath)
        } catch (e: Exception) {
            // CRÍTICO: Restaurar dados originais do usuário
            restoreFromBackup(fileManager, dbPath, backupPath)
            try { reopenDatabase() } catch (_: Exception) {}
            throw DatabaseImportException(
                "Falha ao importar banco de dados. Dados originais foram preservados.",
                e
            )
        }
    }

    // region File Helpers

    private fun getDocumentDirectory(): String {
        @OptIn(ExperimentalForeignApi::class)
        val dir = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )
        return requireNotNull(dir?.path)
    }

    private fun getDbPath(): String = "${getDocumentDirectory()}/$DB_NAME"

    @OptIn(ExperimentalForeignApi::class)
    private fun readFileBytes(path: String): ByteArray {
        val data = NSFileManager.defaultManager.contentsAtPath(path)
            ?: throw DatabaseImportException("Arquivo do banco de dados não encontrado: $path")
        return data.toByteArray()
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun writeFileBytes(path: String, bytes: ByteArray) {
        val nsData = bytes.toNSData()
        if (!nsData.writeToFile(path, atomically = true)) {
            throw DatabaseImportException("Falha ao escrever arquivo: $path")
        }
    }

    // endregion

    // region Backup/Restore

    private fun createSafetyBackup(
        fileManager: NSFileManager,
        dbPath: String,
        backupPath: String
    ) {
        if (!fileManager.fileExistsAtPath(dbPath)) return

        // Remover backup anterior se existir
        if (fileManager.fileExistsAtPath(backupPath)) {
            fileManager.removeItemAtPath(backupPath, null)
        }
        fileManager.copyItemAtPath(dbPath, toPath = backupPath, error = null)

        WAL_SUFFIXES.forEach { suffix ->
            val auxPath = dbPath + suffix
            val auxBackupPath = backupPath + suffix
            if (fileManager.fileExistsAtPath(auxPath)) {
                if (fileManager.fileExistsAtPath(auxBackupPath)) {
                    fileManager.removeItemAtPath(auxBackupPath, null)
                }
                fileManager.copyItemAtPath(auxPath, toPath = auxBackupPath, error = null)
            }
        }
    }

    private fun restoreFromBackup(
        fileManager: NSFileManager,
        dbPath: String,
        backupPath: String
    ) {
        if (!fileManager.fileExistsAtPath(backupPath)) return

        // Limpar arquivos do import falho
        clearWalFiles(fileManager, dbPath)
        if (fileManager.fileExistsAtPath(dbPath)) {
            fileManager.removeItemAtPath(dbPath, null)
        }

        // Restaurar backup
        fileManager.copyItemAtPath(backupPath, toPath = dbPath, error = null)
        WAL_SUFFIXES.forEach { suffix ->
            val auxBackupPath = backupPath + suffix
            if (fileManager.fileExistsAtPath(auxBackupPath)) {
                fileManager.copyItemAtPath(auxBackupPath, toPath = dbPath + suffix, error = null)
            }
        }
        cleanupBackupFiles(fileManager, backupPath)
    }

    private fun clearWalFiles(fileManager: NSFileManager, dbPath: String) {
        WAL_SUFFIXES.forEach { suffix ->
            val auxPath = dbPath + suffix
            if (fileManager.fileExistsAtPath(auxPath)) {
                fileManager.removeItemAtPath(auxPath, null)
            }
        }
    }

    private fun cleanupBackupFiles(fileManager: NSFileManager, backupPath: String) {
        if (fileManager.fileExistsAtPath(backupPath)) {
            fileManager.removeItemAtPath(backupPath, null)
        }
        WAL_SUFFIXES.forEach { suffix ->
            val auxPath = backupPath + suffix
            if (fileManager.fileExistsAtPath(auxPath)) {
                fileManager.removeItemAtPath(auxPath, null)
            }
        }
    }

    // endregion

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
        private const val DB_NAME = "caixa_flavia.db"
        private val WAL_SUFFIXES = listOf("-wal", "-shm")
    }
}

// region NSData <-> ByteArray Extensions

@OptIn(ExperimentalForeignApi::class)
internal fun NSData.toByteArray(): ByteArray {
    if (length.toInt() == 0) return ByteArray(0)
    return ByteArray(length.toInt()).apply {
        usePinned { pinned ->
            memcpy(pinned.addressOf(0), bytes, length)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
internal fun ByteArray.toNSData(): NSData {
    if (isEmpty()) return NSData()
    return memScoped {
        NSData.create(bytes = allocArrayOf(this@toNSData), length = size.toULong())
    }
}

// endregion
