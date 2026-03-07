package filipe.guerreiro.domain.service

import java.awt.Desktop
import java.io.File
import java.io.FileOutputStream

actual class ShareManager actual constructor() {

    actual fun shareXlsxFile(filename: String, content: ByteArray) {
        try {
            val tempDir = File(System.getProperty("java.io.tmpdir"), "ControleDeCaixa_Exports")
            tempDir.mkdirs()
            
            val newFile = File(tempDir, filename)
            val fos = FileOutputStream(newFile)
            fos.write(content)
            fos.close()

            if (Desktop.isDesktopSupported()) {
                val desktop = Desktop.getDesktop()
                if (desktop.isSupported(Desktop.Action.OPEN)) {
                    desktop.open(newFile)
                } else if (desktop.isSupported(Desktop.Action.BROWSE_FILE_DIR)) {
                    desktop.browseFileDirectory(newFile)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
