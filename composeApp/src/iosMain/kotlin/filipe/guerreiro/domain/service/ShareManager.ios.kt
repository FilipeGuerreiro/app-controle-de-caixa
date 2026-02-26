package filipe.guerreiro.domain.service

import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCharacterSet
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.URLPathAllowedCharacterSet
import platform.Foundation.dataUsingEncoding
import platform.Foundation.stringByAddingPercentEncodingWithAllowedCharacters
import platform.Foundation.writeToURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIWindowScene
import platform.UIKit.popoverPresentationController

actual class ShareManager actual constructor() {

    @OptIn(ExperimentalForeignApi::class)
    actual fun shareCsvFile(filename: String, content: String) {
        val data: NSData? = (content as NSString).dataUsingEncoding(NSUTF8StringEncoding)
        if (data == null) return

        val tempDir = NSTemporaryDirectory()
        val formattedFilename = (filename as NSString).stringByAddingPercentEncodingWithAllowedCharacters(NSCharacterSet.URLPathAllowedCharacterSet()) ?: "export.csv"
        val path = tempDir + formattedFilename
        val url = NSURL.fileURLWithPath(path)

        val success = data.writeToURL(url, true)
        
        if (success) {
            val activityViewController = UIActivityViewController(
                activityItems = listOf(url),
                applicationActivities = null
            )
            
            val windowScene = UIApplication.sharedApplication.connectedScenes.firstOrNull() as? UIWindowScene
            val unmanagedWindow = windowScene?.windows?.firstOrNull() as? platform.UIKit.UIWindow
            
            val rootViewController = unmanagedWindow?.rootViewController
            
            // iPad support
            val popover = activityViewController.popoverPresentationController
            if (popover != null && rootViewController?.view != null) {
                popover.sourceView = rootViewController.view
                popover.sourceRect = rootViewController.view.bounds
            }

            rootViewController?.presentViewController(activityViewController, animated = true, completion = null)
        }
    }
}
