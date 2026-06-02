package com.kaduoduo.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface BankDao {
    @Query("SELECT * FROM banks ORDER BY bankId")
    fun observeBanks(): Flow<List<BankEntity>>

    @Query("SELECT COUNT(*) FROM banks")
    suspend fun countBanks(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(banks: List<BankEntity>)
}

@Dao
interface CreditCardDao {
    @Transaction
    @Query("SELECT * FROM credit_cards ORDER BY cardId DESC")
    fun observeCardOverviews(): Flow<List<CardOverview>>

    @Transaction
    @Query("SELECT * FROM credit_cards WHERE cardId = :cardId")
    fun observeCardDetail(cardId: Long): Flow<CardDetail?>

    @Insert
    suspend fun insert(card: CreditCardEntity): Long

    @Query("UPDATE credit_cards SET fixedLimitFen = :fixedLimitFen WHERE cardId = :cardId")
    suspend fun updateFixedLimit(cardId: Long, fixedLimitFen: Long)

    @Query(
        """
        UPDATE credit_cards
        SET temporaryLimitFen = :temporaryLimitFen,
            temporaryLimitExpireEpochDay = :expireEpochDay
        WHERE cardId = :cardId
        """
    )
    suspend fun updateTemporaryLimit(
        cardId: Long,
        temporaryLimitFen: Long?,
        expireEpochDay: Long?
    )
}

@Dao
interface FeeRuleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rule: FeeRuleEntity)

    @Query(
        """
        UPDATE fee_rules
        SET currentCount = currentCount + :count,
            currentAmountFen = currentAmountFen + :amountFen
        WHERE cardId = :cardId
        """
    )
    suspend fun addProgress(cardId: Long, count: Int, amountFen: Long)
}

@Dao
interface LimitHistoryDao {
    @Insert
    suspend fun insert(history: LimitHistoryEntity)
}

@Dao
interface BenefitDao {
    @Insert
    suspend fun insert(benefit: BenefitEntity)

    @Query(
        """
        UPDATE benefits
        SET remainingCount = remainingCount - 1
        WHERE benefitId = :benefitId AND remainingCount > 0
        """
    )
    suspend fun redeemOnce(benefitId: Long)
}
