package filipe.guerreiro.domain.model

import androidx.room.TypeConverter
import kotlin.time.Instant

class Converters {

    @TypeConverter
    fun fromTimeStamp(value: Long?): Instant? {
        return value?.let { Instant.fromEpochMilliseconds(it)}
    }

    @TypeConverter
    fun dateToTimestamp(date: Instant?): Long? {
        return date?.toEpochMilliseconds()
    }

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }
}