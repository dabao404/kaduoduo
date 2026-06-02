package com.kaduoduo

import android.app.Application
import com.kaduoduo.data.local.KaduoduoDatabase
import com.kaduoduo.domain.CardRepository

class KaduoduoApplication : Application() {
    val database: KaduoduoDatabase by lazy {
        KaduoduoDatabase.create(this)
    }

    val repository: CardRepository by lazy {
        CardRepository(
            bankDao = database.bankDao(),
            cardDao = database.creditCardDao(),
            feeRuleDao = database.feeRuleDao(),
            limitHistoryDao = database.limitHistoryDao(),
            benefitDao = database.benefitDao()
        )
    }
}
