package filipe.guerreiro

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.room.Room
import androidx.room.RoomDatabase
import filipe.guerreiro.data.local.AppDatabase
import org.koin.dsl.module
import java.io.File

val desktopDatabaseModule = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        val dbFile = File(System.getProperty("java.io.tmpdir"), "caixa.db")
        Room.databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath
        )
    }
}

fun main() {
    initKoin {
        modules(desktopDatabaseModule)
    }
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "ControleDeCaixa",
        ) {
            App()
        }
    }
}
