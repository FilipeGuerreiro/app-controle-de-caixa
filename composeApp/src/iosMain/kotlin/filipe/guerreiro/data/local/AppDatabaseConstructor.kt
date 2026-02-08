package filipe.guerreiro.data.local

import androidx.room.InvalidationTracker
import androidx.room.RoomDatabaseConstructor
import filipe.guerreiro.data.local.dao.CashDao
import filipe.guerreiro.data.local.dao.TransactionDao
import filipe.guerreiro.data.local.dao.UserDao

object AppDatabaseConstructorPlatform : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase = object : AppDatabase() {
        override fun cashDao(): CashDao = throw NotImplementedError("iOS DB not initialized in this build")
        override fun transactionDao(): TransactionDao = throw NotImplementedError("iOS DB not initialized in this build")
        override fun userDao(): UserDao = throw NotImplementedError("iOS DB not initialized in this build")

        override fun createInvalidationTracker(): InvalidationTracker = throw NotImplementedError("iOS DB not initialized")
    }
}