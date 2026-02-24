package filipe.guerreiro

import androidx.room.Room
import androidx.room.RoomDatabase
import filipe.guerreiro.data.AndroidUserPreferences
import filipe.guerreiro.data.SessionPreferences
import filipe.guerreiro.data.UserPreferences
import filipe.guerreiro.data.local.AppDatabase
import filipe.guerreiro.session.AndroidSessionPreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidDatabaseModule = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        val context = androidContext()
        val dbFile = context.getDatabasePath("caixa.db")
        Room.databaseBuilder<AppDatabase>(
            context = context,
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

val androidModule = module {
    single<UserPreferences> {
        AndroidUserPreferences(get())
    }

    single<SessionPreferences> {
        AndroidSessionPreferences(get())
    }
}