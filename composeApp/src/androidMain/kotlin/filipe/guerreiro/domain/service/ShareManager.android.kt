package filipe.guerreiro.domain.service

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import org.koin.java.KoinJavaComponent.getKoin
import java.io.File
import java.io.FileOutputStream

actual class ShareManager {

    private val context: Context get() = getKoin().get()

    actual fun shareXlsxFile(filename: String, content: ByteArray) {
        val cacheDir = File(context.cacheDir, "report_exports").also { it.mkdirs() }
        val file = File(cacheDir, filename)
        FileOutputStream(file).use { fos ->
            fos.write(content)
        }
        shareFile(file, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    }

    private fun shareFile(file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(
            Intent.createChooser(intent, "Compartilhar relatório")
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
