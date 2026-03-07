package filipe.guerreiro

import androidx.room.Room
import androidx.room.RoomDatabase
import filipe.guerreiro.data.IosUserPreferences
import filipe.guerreiro.data.SessionPreferences
import filipe.guerreiro.data.UserPreferences
import filipe.guerreiro.data.local.AppDatabase
import filipe.guerreiro.di.appModule
import filipe.guerreiro.di.databaseDependenciesModule
import filipe.guerreiro.domain.service.backup.DatabaseFileManager
import filipe.guerreiro.domain.service.backup.IosDatabaseFileManager
import filipe.guerreiro.session.IosSessionPreferences
import filipe.guerreiro.data.local.AppDatabaseConstructor
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.context.startKoin
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
val iosDatabaseModule = module {
    factory<RoomDatabase.Builder<AppDatabase>> {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )
        val path = requireNotNull(documentDirectory?.path) + "/caixa_flavia.db"
        Room.databaseBuilder<AppDatabase>(
            name = path,
            factory = { AppDatabaseConstructor.initialize() }
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

val iosModule = module {
    single<UserPreferences> {
        IosUserPreferences()
    }

    single<SessionPreferences> {
        IosSessionPreferences()
    }

    single<DatabaseFileManager> {
        IosDatabaseFileManager()
    }
}


object KoinInitializer {
    fun initialize() {
        startKoin {
            modules(
                appModule, // commonModule
                databaseDependenciesModule,
                iosDatabaseModule,
                iosModule
            )
        }
    }
}