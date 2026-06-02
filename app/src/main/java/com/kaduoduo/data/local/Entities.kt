package com.kaduoduo.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class FeeCycleType { NATURAL_YEAR, ISSUE_DATE_YEAR }
enum class FeeConditionType { SPEND_COUNT, SPEND_AMOUNT, BOTH }
enum class LimitType { FIXED, TEMPORARY }

@Entity(tableName = "banks")
data class BankEntity(
    @PrimaryKey(autoGenerate = true) val bankId: Long = 0,
    val name: String,
    // 存资源名，后续接入真实银行 Logo 时可按名称映射到 drawable。
    val iconResName: String? = null,
    val isCustom: Boolean = false
)

@Entity(
    tableName = "credit_cards",
    foreignKeys = [
        ForeignKey(
            entity = BankEntity::class,
            parentColumns = ["bankId"],
            childColumns = ["bankId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("bankId")]
)
data class CreditCardEntity(
    @PrimaryKey(autoGenerate = true) val cardId: Long = 0,
    val bankId: Long?,
    val customBankName: String? = null,
    val cardName: String,
    val issueDateEpochDay: Long,
    val billingDay: Int,
    val repaymentDay: Int,
    // 金额统一用“分”保存，避免浮点数精度问题。
    val fixedLimitFen: Long,
    val temporaryLimitFen: Long? = null,
    val temporaryLimitExpireEpochDay: Long? = null
)

@Entity(
    tableName = "fee_rules",
    foreignKeys = [
        ForeignKey(
            entity = CreditCardEntity::class,
            parentColumns = ["cardId"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cardId"], unique = true)]
)
data class FeeRuleEntity(
    @PrimaryKey(autoGenerate = true) val ruleId: Long = 0,
    val cardId: Long,
    val cycleType: FeeCycleType,
    val conditionType: FeeConditionType,
    val requiredAmountFen: Long? = null,
    val requiredCount: Int? = null,
    val currentAmountFen: Long = 0,
    val currentCount: Int = 0
)

@Entity(
    tableName = "limit_histories",
    foreignKeys = [
        ForeignKey(
            entity = CreditCardEntity::class,
            parentColumns = ["cardId"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("cardId")]
)
data class LimitHistoryEntity(
    @PrimaryKey(autoGenerate = true) val historyId: Long = 0,
    val cardId: Long,
    val adjustDateEpochDay: Long,
    val type: LimitType,
    val beforeLimitFen: Long?,
    val afterLimitFen: Long,
    val temporaryExpireEpochDay: Long? = null
)

@Entity(
    tableName = "benefits",
    foreignKeys = [
        ForeignKey(
            entity = CreditCardEntity::class,
            parentColumns = ["cardId"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("cardId")]
)
data class BenefitEntity(
    @PrimaryKey(autoGenerate = true) val benefitId: Long = 0,
    val cardId: Long,
    val name: String,
    val totalCount: Int,
    val remainingCount: Int,
    val expireDateEpochDay: Long
)

data class CardOverview(
    @Embedded val card: CreditCardEntity,
    @Relation(parentColumn = "bankId", entityColumn = "bankId")
    val bank: BankEntity?,
    @Relation(parentColumn = "cardId", entityColumn = "cardId")
    val feeRule: FeeRuleEntity?
)

data class CardDetail(
    @Embedded val card: CreditCardEntity,
    @Relation(parentColumn = "bankId", entityColumn = "bankId")
    val bank: BankEntity?,
    @Relation(parentColumn = "cardId", entityColumn = "cardId")
    val feeRule: FeeRuleEntity?,
    @Relation(parentColumn = "cardId", entityColumn = "cardId")
    val limitHistories: List<LimitHistoryEntity>,
    @Relation(parentColumn = "cardId", entityColumn = "cardId")
    val benefits: List<BenefitEntity>
)
