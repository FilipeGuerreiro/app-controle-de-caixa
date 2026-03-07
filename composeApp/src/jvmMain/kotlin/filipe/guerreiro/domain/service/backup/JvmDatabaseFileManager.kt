package filipe.guerreiro.domain.service.backup

class JvmDatabaseFileManager : DatabaseFileManager {

    override suspend fun exportDatabase(): ByteArray {
        throw UnsupportedOperationException(
            "Backup em nuvem não é suportado na plataforma Desktop/JVM"
        )
    }

    override suspend fun importDatabase(data: ByteArray) {
        throw UnsupportedOperationException(
            "Backup em nuvem não é suportado na plataforma Desktop/JVM"
        )
    }
}
