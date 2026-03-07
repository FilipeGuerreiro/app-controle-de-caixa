package filipe.guerreiro.domain.service.backup

import filipe.guerreiro.domain.model.backup.CloudBackup
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

/**
 * Cliente REST para a Google Drive API v3 usando o escopo restrito appDataFolder.
 *
 * O appDataFolder é uma pasta oculta no Google Drive do usuário, visível
 * apenas por este aplicativo. Cada app tem seu próprio espaço isolado.
 *
 * Todas as operações requerem um Bearer Token OAuth válido com o escopo
 * https://www.googleapis.com/auth/drive.appdata
 */
class GoogleDriveClient(
    private val httpClient: HttpClient
) {
    companion object {
        private const val DRIVE_API_BASE = "https://www.googleapis.com/drive/v3/files"
        private const val DRIVE_UPLOAD_BASE = "https://www.googleapis.com/upload/drive/v3/files"
        const val MAX_BACKUPS = 5
    }

    /**
     * Lista todos os backups (.db) no appDataFolder, ordenados do mais recente ao mais antigo.
     */
    suspend fun listBackups(accessToken: String): List<CloudBackup> {
        val response = httpClient.get(DRIVE_API_BASE) {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            parameter("spaces", "appDataFolder")
            parameter("fields", "files(id,name,size,createdTime)")
            parameter("orderBy", "createdTime desc")
            parameter("q", "name contains 'backup_' and name contains '.db'")
        }

        if (response.status != HttpStatusCode.OK) {
            val errorBody = response.bodyAsText()
            throw DriveApiException("Falha ao listar backups (${response.status}): $errorBody")
        }

        val json = Json.parseToJsonElement(response.bodyAsText())
        val files = json.jsonObject["files"]?.jsonArray ?: return emptyList()

        return files.mapNotNull { element ->
            val obj = element.jsonObject
            val id = obj["id"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val name = obj["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val size = obj["size"]?.jsonPrimitive?.longOrNull ?: 0L
            val createdTimeStr = obj["createdTime"]?.jsonPrimitive?.content

            val createdAt = createdTimeStr?.let {
                try {
                    Instant.parse(it)
                } catch (_: Exception) {
                    null
                }
            } ?: Instant.DISTANT_PAST

            CloudBackup(
                fileId = id,
                fileName = name,
                sizeBytes = size,
                createdAt = createdAt
            )
        }
    }

    /**
     * Faz upload de um backup para o appDataFolder usando multipart/related.
     *
     * O Google Drive API exige multipart/related (não form-data) para enviar
     * metadados JSON + conteúdo binário na mesma requisição.
     *
     * @param accessToken Token OAuth válido
     * @param fileName Nome do arquivo (ex: "backup_2026_03_07_143000.db")
     * @param data Bytes do arquivo .db exportado pelo DatabaseFileManager
     * @return CloudBackup com os metadados do arquivo criado no Drive
     */
    suspend fun uploadBackup(
        accessToken: String,
        fileName: String,
        data: ByteArray
    ): CloudBackup {
        val boundary = "backup_boundary_${kotlin.random.Random.nextLong().toULong()}"

        val metadata = """{"name":"$fileName","parents":["appDataFolder"]}"""

        // Construir o body multipart/related manualmente (texto + binário)
        val prefix = buildString {
            append("--$boundary\r\n")
            append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
            append(metadata)
            append("\r\n--$boundary\r\n")
            append("Content-Type: application/octet-stream\r\n\r\n")
        }.encodeToByteArray()
        val suffix = "\r\n--$boundary--\r\n".encodeToByteArray()

        val body = prefix + data + suffix

        val response = httpClient.post("$DRIVE_UPLOAD_BASE?uploadType=multipart") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            contentType(ContentType.parse("multipart/related; boundary=$boundary"))
            setBody(body)
        }

        if (response.status != HttpStatusCode.OK) {
            val errorBody = response.bodyAsText()
            throw DriveApiException("Falha ao enviar backup (${response.status}): $errorBody")
        }

        val json = Json.parseToJsonElement(response.bodyAsText())
        val obj = json.jsonObject
        val fileId = obj["id"]?.jsonPrimitive?.content
            ?: throw DriveApiException("Resposta do Drive sem ID do arquivo")

        return CloudBackup(
            fileId = fileId,
            fileName = fileName,
            sizeBytes = data.size.toLong(),
            createdAt = Clock.System.now()
        )
    }

    /**
     * Faz download dos bytes de um backup específico do Drive.
     *
     * @param accessToken Token OAuth válido
     * @param fileId ID do arquivo no Google Drive
     * @return ByteArray com o conteúdo do arquivo .db
     */
    suspend fun downloadBackup(accessToken: String, fileId: String): ByteArray {
        val response = httpClient.get("$DRIVE_API_BASE/$fileId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            parameter("alt", "media")
        }

        if (response.status != HttpStatusCode.OK) {
            val errorBody = response.bodyAsText()
            throw DriveApiException("Falha ao baixar backup (${response.status}): $errorBody")
        }

        return response.body()
    }

    /**
     * Remove um arquivo do appDataFolder.
     *
     * @param accessToken Token OAuth válido
     * @param fileId ID do arquivo a ser deletado
     */
    suspend fun deleteFile(accessToken: String, fileId: String) {
        val response = httpClient.delete("$DRIVE_API_BASE/$fileId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }

        // Google Drive retorna 204 No Content em delete bem sucedido
        if (response.status != HttpStatusCode.NoContent && response.status != HttpStatusCode.OK) {
            val errorBody = response.bodyAsText()
            throw DriveApiException("Falha ao deletar backup (${response.status}): $errorBody")
        }
    }

    /**
     * Remove backups antigos que excedam MAX_BACKUPS.
     * A lista já vem ordenada por createdTime desc, então descartamos os mais antigos.
     *
     * @return Número de backups deletados
     */
    suspend fun rotateBackups(accessToken: String): Int {
        val backups = listBackups(accessToken)
        if (backups.size <= MAX_BACKUPS) return 0

        val toDelete = backups.drop(MAX_BACKUPS)
        var deleted = 0
        for (backup in toDelete) {
            try {
                deleteFile(accessToken, backup.fileId)
                deleted++
            } catch (e: DriveApiException) {
                // Log mas não interrompe a rotação por falha em um arquivo individual
                println("Aviso: falha ao deletar backup antigo ${backup.fileName}: ${e.message}")
            }
        }
        return deleted
    }
}

/**
 * Exceção específica para erros na comunicação com a Google Drive API.
 */
class DriveApiException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
