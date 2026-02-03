package filipe.guerreiro

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import filipe.guerreiro.data.local.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val androidDatabaseModule = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        val context = androidContext()
        val dbFile = context.getDatabasePath("caixa.db")
        Room.databaseBuilder<AppDatabase>(
            context = context,
            name = dbFile.absolutePath
        )
    }
}