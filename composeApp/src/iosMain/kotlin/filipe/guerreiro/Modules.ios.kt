package filipe.guerreiro

import androidx.room.Room
import androidx.room.RoomDatabase
import filipe.guerreiro.data.local.AppDatabase
import filipe.guerreiro.di.appModule
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.context.startKoin
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
val iosDatabaseModule = module {
    single<RoomDatabase.Builder<AppDatabase>> {
        val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )
        val path = documentDirectory?.path + "/caixa_flavia.db"
        Room.databaseBuilder<AppDatabase>(
            name = path,
//            factory = { AppDatabase::class.instantiateImpl() }
        )
    }
}


object KoinInitializer {
    fun initialize() {
        startKoin {
            modules(appModule, iosDatabaseModule)
        }
    }
}