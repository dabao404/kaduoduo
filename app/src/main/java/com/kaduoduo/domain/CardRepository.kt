package com.kaduoduo.domain

import com.kaduoduo.data.local.BankDao
import com.kaduoduo.data.local.BankEntity
import com.kaduoduo.data.local.BenefitDao
import com.kaduoduo.data.local.BenefitEntity
import com.kaduoduo.data.local.CardDetail
import com.kaduoduo.data.local.CardOverview
import com.kaduoduo.data.local.CreditCardDao
import com.kaduoduo.data.local.CreditCardEntity
import com.kaduoduo.data.local.FeeRuleDao
import com.kaduoduo.data.local.FeeRuleEntity
import com.kaduoduo.data.local.LimitHistoryDao
import com.kaduoduo.data.local.LimitHistoryEntity
import com.kaduoduo.data.local.LimitType
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class CardRepository(
    private val bankDao: BankDao,
    private val cardDao: CreditCardDao,
    private val feeRuleDao: FeeRuleDao,
    private val limitHistoryDao: LimitHistoryDao,
    private val benefitDao: BenefitDao
) {
    val cards: Flow<List<CardOverview>> = cardDao.observeCardOverviews()
    val banks: Flow<List<BankEntity>> = bankDao.observeBanks()

    fun observeDetail(cardId: Long): Flow<CardDetail?> {
        return cardDao.observeCardDetail(cardId)
    }

    suspend fun seedDefaultBanks() {
        if (bankDao.countBanks() > 0) return

        bankDao.insertAll(
            listOf(
                BankEntity(name = "招商银行", iconResName = "ic_bank_cmb"),
                BankEntity(name = "交通银行", iconResName = "ic_bank_bocom"),
                BankEntity(name = "建设银行", iconResName = "ic_bank_ccb"),
                BankEntity(name = "平安银行", iconResName = "ic_bank_pingan"),
                BankEntity(name = "工商银行", iconResName = "ic_bank_icbc"),
                BankEntity(name = "农业银行", iconResName = "ic_bank_abc"),
                BankEntity(name = "中国银行", iconResName = "ic_bank_boc"),
                BankEntity(name = "中信银行", iconResName = "ic_bank_citic")
            )
        )
    }

    suspend fun addCard(
        card: CreditCardEntity,
        feeRuleBuilder: (Long) -> FeeRuleEntity,
        initialHistoryBuilder: (Long) -> LimitHistoryEntity
    ) {
        val cardId = cardDao.insert(card)
        feeRuleDao.upsert(feeRuleBuilder(cardId))
        limitHistoryDao.insert(initialHistoryBuilder(cardId))
    }

    suspend fun addAnnualFeeProgress(cardId: Long, count: Int, amountFen: Long) {
        feeRuleDao.addProgress(cardId, count, amountFen)
    }

    suspend fun addLimitHistory(
        detail: CardDetail,
        type: LimitType,
        afterLimitFen: Long,
        temporaryExpireEpochDay: Long? = null
    ) {
        val beforeLimitFen = when (type) {
            LimitType.FIXED -> detail.card.fixedLimitFen
            LimitType.TEMPORARY -> detail.card.temporaryLimitFen
        }

        limitHistoryDao.insert(
            LimitHistoryEntity(
                cardId = detail.card.cardId,
                adjustDateEpochDay = LocalDate.now().toEpochDay(),
                type = type,
                beforeLimitFen = beforeLimitFen,
                afterLimitFen = afterLimitFen,
                temporaryExpireEpochDay = temporaryExpireEpochDay
            )
        )

        when (type) {
            LimitType.FIXED -> cardDao.updateFixedLimit(detail.card.cardId, afterLimitFen)
            LimitType.TEMPORARY -> cardDao.updateTemporaryLimit(
                detail.card.cardId,
                afterLimitFen,
                temporaryExpireEpochDay
            )
        }
    }

    suspend fun addBenefit(
        cardId: Long,
        name: String,
        totalCount: Int,
        expireDateEpochDay: Long
    ) {
        benefitDao.insert(
            BenefitEntity(
                cardId = cardId,
                name = name,
                totalCount = totalCount,
                remainingCount = totalCount,
                expireDateEpochDay = expireDateEpochDay
            )
        )
    }

    suspend fun redeemBenefit(benefitId: Long) {
        benefitDao.redeemOnce(benefitId)
    }
}
