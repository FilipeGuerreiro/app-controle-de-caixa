package filipe.guerreiro.data.local

import androidx.room.InvalidationTracker
import androidx.room.RoomDatabaseConstructor
import filipe.guerreiro.data.local.dao.CashDao
import filipe.guerreiro.data.local.dao.CategoryDao
import filipe.guerreiro.data.local.dao.PaymentMethodDao
import filipe.guerreiro.data.local.dao.TransactionDao
import filipe.guerreiro.data.local.dao.UserDao

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
actual object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase = AppDatabase_Impl()
}