package com.kaduoduo.ui

import com.kaduoduo.data.local.FeeConditionType
import com.kaduoduo.data.local.FeeRuleEntity
import java.time.LocalDate

fun Long.toYuanText(): String = "¥%.2f".format(this / 100.0)

fun Long.toDateText(): String = LocalDate.ofEpochDay(this).toString()

fun FeeRuleEntity.annualFeeProgress(): Float {
    val countProgress = requiredCount?.takeIf { it > 0 }
        ?.let { currentCount.toFloat() / it }
    val amountProgress = requiredAmountFen?.takeIf { it > 0 }
        ?.let { currentAmountFen.toFloat() / it }

    return when (conditionType) {
        FeeConditionType.SPEND_COUNT -> countProgress ?: 0f
        FeeConditionType.SPEND_AMOUNT -> amountProgress ?: 0f
        FeeConditionType.BOTH -> listOfNotNull(countProgress, amountProgress).minOrNull() ?: 0f
    }.coerceIn(0f, 1f)
}
