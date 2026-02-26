package filipe.guerreiro.domain.service

import java.awt.Desktop
import java.io.File
import java.io.FileOutputStream

actual class ShareManager actual constructor() {

    actual fun shareCsvFile(filename: String, content: String) {
        try {
            // No Desktop, salvamos na pasta temporária do sistema operacional
            val tempDir = File(System.getProperty("java.io.tmpdir"), "ControleDeCaixa_Exports")
            tempDir.mkdirs()
            
            val newFile = File(tempDir, filename)
            val fos = FileOutputStream(newFile)
            fos.write(content.toByteArray(Charsets.UTF_8))
            fos.close()

            // Tenta abrir o arquivo com o programa padrão do sistema (ex: Excel) ou a pasta
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
