package filipe.guerreiro

import androidx.room.Room
import androidx.room.RoomDatabase
import filipe.guerreiro.data.AndroidUserPreferences
import filipe.guerreiro.data.SessionPreferences
import filipe.guerreiro.data.UserPreferences
import filipe.guerreiro.data.local.AppDatabase
import filipe.guerreiro.data.local.ALL_V7_MIGRATIONS
import filipe.guerreiro.domain.service.backup.AndroidDatabaseFileManager
import filipe.guerreiro.domain.service.backup.DatabaseFileManager
import filipe.guerreiro.session.AndroidSessionPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidDatabaseModule = module {
    factory<RoomDatabase.Builder<AppDatabase>> {
        val context = androidContext()
        val dbFile = context.getDatabasePath("caixa.db")
        Room.databaseBuilder<AppDatabase>(
            context = context,
            name = dbFile.absolutePath
        ).addMigrations(*ALL_V7_MIGRATIONS)
    }
}

val androidModule = module {
    single<UserPreferences> {
        AndroidUserPreferences(get())
    }

    single<SessionPreferences> {
        AndroidSessionPreferences(get())
    }

    single<DatabaseFileManager> {
        AndroidDatabaseFileManager(androidContext())
    }
}