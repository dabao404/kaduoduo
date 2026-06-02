package com.kaduoduo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

class KaduoduoConverters {
    @TypeConverter
    fun feeCycleTypeToString(value: FeeCycleType): String = value.name

    @TypeConverter
    fun stringToFeeCycleType(value: String): FeeCycleType = FeeCycleType.valueOf(value)

    @TypeConverter
    fun feeConditionTypeToString(value: FeeConditionType): String = value.name

    @TypeConverter
    fun stringToFeeConditionType(value: String): FeeConditionType = FeeConditionType.valueOf(value)

    @TypeConverter
    fun limitTypeToString(value: LimitType): String = value.name

    @TypeConverter
    fun stringToLimitType(value: String): LimitType = LimitType.valueOf(value)
}

@Database(
    entities = [
        BankEntity::class,
        CreditCardEntity::class,
        FeeRuleEntity::class,
        LimitHistoryEntity::class,
        BenefitEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(KaduoduoConverters::class)
abstract class KaduoduoDatabase : RoomDatabase() {
    abstract fun bankDao(): BankDao
    abstract fun creditCardDao(): CreditCardDao
    abstract fun feeRuleDao(): FeeRuleDao
    abstract fun limitHistoryDao(): LimitHistoryDao
    abstract fun benefitDao(): BenefitDao

    companion object {
        fun create(context: Context): KaduoduoDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                KaduoduoDatabase::class.java,
                "kaduoduo.db"
            ).build()
        }
    }
}
