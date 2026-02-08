package filipe.guerreiro.domain.model

import androidx.room.TypeConverter
import kotlin.time.Instant
import kotlin.math.abs
import kotlin.math.round

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

    @TypeConverter
    fun fromCashStatusType(value: CashStatusType): String {
        return value.name
    }

    @TypeConverter
    fun toCashStatusType(value: String): CashStatusType {
        return CashStatusType.valueOf(value)
    }
}