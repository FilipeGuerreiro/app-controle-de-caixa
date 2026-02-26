package filipe.guerreiro.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

/*
// Exemplo de Script de Migração Manual para proteger dados em produção
val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
    override fun migrate(connection: androidx.sqlite.SQLiteConnection) {
        connection.execSQL("ALTER TABLE Category ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0")
    }
}
*/

fun getDatabaseBuilder(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
    return builder
        // Remover/Comentar o destructiveMigration em produção para não apagar o banco dos usuários!
        // .fallbackToDestructiveMigration(true)
        
        // Exemplo: Sempre que aumentar a `version` lá no AppDatabase.kt, registre a migração aqui:
        // .addMigrations(MIGRATION_7_8)
        
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}