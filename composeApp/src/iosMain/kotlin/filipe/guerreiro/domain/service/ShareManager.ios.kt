package filipe.guerreiro.domain.service

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSData
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.writeToFile
import platform.Foundation.create
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.UIKit.popoverPresentationController

actual class ShareManager {

    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual fun shareXlsxFile(filename: String, content: ByteArray) {
        dispatch_async(dispatch_get_main_queue()) {
            val tmpDir = NSTemporaryDirectory()
            val filePath = "$tmpDir$filename"

            val data = content.usePinned { pinned ->
                NSData.create(bytes = pinned.addressOf(0), length = content.size.toULong())
            }
            
            data.writeToFile(filePath, atomically = true)

            val fileUrl = NSURL.fileURLWithPath(filePath)
            val activityVC = UIActivityViewController(
                activityItems = listOf(fileUrl),
                applicationActivities = null
            )

            val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController
            // iPad crash prevention: set popover source view
            activityVC.popoverPresentationController?.sourceView = rootVC?.view
            activityVC.popoverPresentationController?.sourceRect = platform.CoreGraphics.CGRectMake(0.0, 0.0, 0.0, 0.0)

            rootVC?.presentViewController(activityVC, animated = true, completion = null)
        }
    }
}
