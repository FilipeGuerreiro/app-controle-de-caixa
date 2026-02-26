package filipe.guerreiro.domain.service

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.FileOutputStream

actual class ShareManager actual constructor() : KoinComponent {

    private val context: Context by inject()

    actual fun shareCsvFile(filename: String, content: String) {
        try {
            val cachePath = File(context.cacheDir, "csv_exports")
            cachePath.mkdirs()
            
            val newFile = File(cachePath, filename)
            val fos = FileOutputStream(newFile)
            fos.write(content.toByteArray(Charsets.UTF_8))
            fos.close()

            val authority = "${context.packageName}.fileprovider"
            val fileUri = FileProvider.getUriForFile(context, authority, newFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Compartilhar CSV").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooserIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
