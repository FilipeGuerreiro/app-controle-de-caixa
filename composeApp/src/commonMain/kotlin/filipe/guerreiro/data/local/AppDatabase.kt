package filipe.guerreiro.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import filipe.guerreiro.data.local.dao.CashDao
import filipe.guerreiro.data.local.dao.TransactionDao
import filipe.guerreiro.domain.model.CashSession
import filipe.guerreiro.domain.model.Converters
import filipe.guerreiro.domain.model.Transaction


@Database(
    entities = [CashSession::class, Transaction::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cashDao(): CashDao
    abstract fun transactionDao(): TransactionDao
}
