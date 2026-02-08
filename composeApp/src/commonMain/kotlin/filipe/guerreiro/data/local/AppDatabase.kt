package filipe.guerreiro.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import filipe.guerreiro.data.local.dao.CashDao
import filipe.guerreiro.data.local.dao.TransactionDao
import filipe.guerreiro.data.local.dao.UserDao
import filipe.guerreiro.domain.model.CashSession
import filipe.guerreiro.domain.model.Category
import filipe.guerreiro.domain.model.Converters
import filipe.guerreiro.domain.model.PaymentMethod
import filipe.guerreiro.domain.model.Transaction
import filipe.guerreiro.domain.model.User


@Database(
    entities = [
        CashSession::class,
        Transaction::class,
        Category::class,
        PaymentMethod::class,
        User::class
               ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cashDao(): CashDao
    abstract fun transactionDao(): TransactionDao

    abstract fun userDao(): UserDao
}
