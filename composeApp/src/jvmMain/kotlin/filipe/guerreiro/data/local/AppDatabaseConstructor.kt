package filipe.guerreiro.data.local

import androidx.room.RoomDatabaseConstructor

object AppDatabaseConstructorPlatform : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase = AppDatabase_Impl()
}