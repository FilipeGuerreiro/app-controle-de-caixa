package filipe.guerreiro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    companion object {
        var currentActivity: ComponentActivity? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        currentActivity = this
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }

    override fun onDestroy() {
        if (currentActivity == this) currentActivity = null
        super.onDestroy()
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}