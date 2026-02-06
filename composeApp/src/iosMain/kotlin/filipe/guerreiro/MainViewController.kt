package filipe.guerreiro

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

fun MainViewController() : UIViewController {
    KoinInitializer.initialize()

    return ComposeUIViewController {
        App()
    }
}