package filipe.guerreiro

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.room.Room
import androidx.room.RoomDatabase
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
        ).addMigrations(MIGRATION_4_5)
    }
}

val MIGRATION_4_5 = object : androidx.room.migration.Migration(4, 5) {
    override fun migrate(connection: androidx.sqlite.SQLiteConnection) {
        connection.prepare("ALTER TABLE cash_sessions ADD COLUMN dailyGoalAmount INTEGER DEFAULT NULL").use { stmt ->
            stmt.step()
        }
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
