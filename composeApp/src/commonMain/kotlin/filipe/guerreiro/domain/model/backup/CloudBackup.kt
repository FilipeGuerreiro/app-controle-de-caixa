package filipe.guerreiro.domain.model.backup

import kotlin.time.Instant

/**
 * Representação de um backup armazenado no Google Drive (appDataFolder).
 * Mapeado a partir da resposta da Google Drive REST API.
 */
data class CloudBackup(
    val fileId: String,
    val fileName: String,
    val sizeBytes: Long,
    val createdAt: Instant
)
