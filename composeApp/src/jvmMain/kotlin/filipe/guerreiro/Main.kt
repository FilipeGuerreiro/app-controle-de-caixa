package filipe.guerreiro

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.room.Room
import androidx.room.RoomDatabase
import filipe.guerreiro.data.local.ALL_V7_MIGRATIONS
import filipe.guerreiro.data.local.AppDatabase
import filipe.guerreiro.di.databaseDependenciesModule
import org.koin.dsl.module
import filipe.guerreiro.domain.service.backup.DatabaseFileManager
import filipe.guerreiro.domain.service.backup.JvmDatabaseFileManager
import java.io.File

val desktopModule = module {
    single<DatabaseFileManager> { JvmDatabaseFileManager() }
}

val desktopDatabaseModule = module {
    factory<RoomDatabase.Builder<AppDatabase>> {
        val dbFile = File(System.getProperty("java.io.tmpdir"), "caixa.db")
        Room.databaseBuilder<AppDatabase>(
            name = dbFile.absolutePath
        ).addMigrations(*ALL_V7_MIGRATIONS)
    }
}

fun main() {
    initKoin {
        modules(desktopDatabaseModule, databaseDependenciesModule, desktopModule)
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
