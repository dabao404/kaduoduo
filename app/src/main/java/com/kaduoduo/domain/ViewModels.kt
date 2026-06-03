package com.kaduoduo.domain

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kaduoduo.data.local.BankEntity
import com.kaduoduo.data.local.CardDetail
import com.kaduoduo.data.local.CardOverview
import com.kaduoduo.data.local.CreditCardEntity
import com.kaduoduo.data.local.FeeConditionType
import com.kaduoduo.data.local.FeeCycleType
import com.kaduoduo.data.local.FeeRuleEntity
import com.kaduoduo.data.local.LimitHistoryEntity
import com.kaduoduo.data.local.LimitType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    val cards: List<CardOverview> = emptyList(),
    val isEmpty: Boolean = true
)

class HomeViewModel(
    private val repository: CardRepository
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> =
        repository.cards
            .map { cards -> HomeUiState(cards = cards, isEmpty = cards.isEmpty()) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    init {
        viewModelScope.launch {
            repository.seedDefaultBanks()
        }
    }
}

data class AddCardUiState(
    val banks: List<BankEntity> = emptyList(),
    val selectedBankId: Long? = null,
    val customBankName: String = "",
    val cardName: String = "",
    val issueDateText: String = LocalDate.now().toString(),
    val billingDay: String = "1",
    val repaymentDay: String = "20",
    val fixedLimitYuan: String = "",
    val temporaryLimitYuan: String = "",
    val temporaryExpireDateText: String = "",
    val cycleType: FeeCycleType = FeeCycleType.NATURAL_YEAR,
    val conditionType: FeeConditionType = FeeConditionType.SPEND_COUNT,
    val requiredCount: String = "12",
    val requiredAmountYuan: String = "",
    val errorMessage: String? = null,
    val saved: Boolean = false
)

class AddCardViewModel(
    private val repository: CardRepository
) : ViewModel() {
    private val formState = MutableStateFlow(AddCardUiState())

    val uiState: StateFlow<AddCardUiState> =
        repository.banks
            .combine(formState) { banks, form ->
                form.copy(banks = banks)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AddCardUiState())

    fun update(reducer: AddCardUiState.() -> AddCardUiState) {
        formState.update { it.reducer().copy(errorMessage = null) }
    }

    fun save(
        state: AddCardUiState,
        onSaved: () -> Unit
    ) {
        val cardName = state.cardName.trim()
        val fixedLimitFen = state.fixedLimitYuan.toFenOrNull()
        val issueDate = state.issueDateText.toLocalDateOrNull()
        val billingDay = state.billingDay.toIntOrNull()
        val repaymentDay = state.repaymentDay.toIntOrNull()

        if (cardName.isBlank() || fixedLimitFen == null || issueDate == null ||
            billingDay !in 1..31 || repaymentDay !in 1..31
        ) {
            formState.update { it.copy(errorMessage = "请检查卡片名称、日期、账单日、还款日和固定额度。") }
            return
        }

        viewModelScope.launch {
            val temporaryLimitFen = state.temporaryLimitYuan.toFenOrNull()
            val temporaryExpire = state.temporaryExpireDateText
                .takeIf { it.isNotBlank() }
                ?.toLocalDateOrNull()
                ?.toEpochDay()

            val card = CreditCardEntity(
                bankId = state.selectedBankId,
                customBankName = state.customBankName.trim().ifBlank { null },
                cardName = cardName,
                issueDateEpochDay = issueDate.toEpochDay(),
                billingDay = billingDay!!,
                repaymentDay = repaymentDay!!,
                fixedLimitFen = fixedLimitFen,
                temporaryLimitFen = temporaryLimitFen,
                temporaryLimitExpireEpochDay = temporaryExpire
            )

            repository.addCard(
                card = card,
                feeRuleBuilder = { cardId ->
                    FeeRuleEntity(
                        cardId = cardId,
                        cycleType = state.cycleType,
                        conditionType = state.conditionType,
                        requiredAmountFen = state.requiredAmountYuan.toFenOrNull(),
                        requiredCount = state.requiredCount.toIntOrNull()
                    )
                },
                initialHistoryBuilder = { cardId ->
                    LimitHistoryEntity(
                        cardId = cardId,
                        adjustDateEpochDay = issueDate.toEpochDay(),
                        type = LimitType.FIXED,
                        beforeLimitFen = null,
                        afterLimitFen = fixedLimitFen
                    )
                }
            )
            formState.update { AddCardUiState(saved = true) }
            onSaved()
        }
    }
}

data class DetailUiState(
    val detail: CardDetail? = null,
    val isLoading: Boolean = true
)

class CardDetailViewModel(
    private val cardId: Long,
    private val repository: CardRepository
) : ViewModel() {
    val uiState: StateFlow<DetailUiState> =
        repository.observeDetail(cardId)
            .map { DetailUiState(detail = it, isLoading = false) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetailUiState())

    fun redeemBenefit(benefitId: Long) {
        viewModelScope.launch {
            repository.redeemBenefit(benefitId)
        }
    }

    fun addFixedLimitHistory(afterLimitYuan: String) {
        val detail = uiState.value.detail ?: return
        val afterLimitFen = afterLimitYuan.toFenOrNull() ?: return
        viewModelScope.launch {
            repository.addLimitHistory(
                detail = detail,
                type = LimitType.FIXED,
                afterLimitFen = afterLimitFen
            )
        }
    }

    fun addTemporaryLimitHistory(afterLimitYuan: String, expireDateText: String) {
        val detail = uiState.value.detail ?: return
        val afterLimitFen = afterLimitYuan.toFenOrNull() ?: return
        val expireEpochDay = expireDateText.toLocalDateOrNull()?.toEpochDay()
        viewModelScope.launch {
            repository.addLimitHistory(
                detail = detail,
                type = LimitType.TEMPORARY,
                afterLimitFen = afterLimitFen,
                temporaryExpireEpochDay = expireEpochDay
            )
        }
    }

    fun addBenefit(name: String, totalCountText: String, expireDateText: String) {
        val detail = uiState.value.detail ?: return
        val totalCount = totalCountText.toIntOrNull()?.takeIf { it > 0 } ?: return
        val expireEpochDay = expireDateText.toLocalDateOrNull()?.toEpochDay() ?: return
        val benefitName = name.trim().takeIf { it.isNotBlank() } ?: return

        viewModelScope.launch {
            repository.addBenefit(
                cardId = detail.card.cardId,
                name = benefitName,
                totalCount = totalCount,
                expireDateEpochDay = expireEpochDay
            )
        }
    }
}

class KaduoduoViewModelFactory(
    private val repository: CardRepository,
    private val cardId: Long? = null
) : ViewModelProvider.Factory {
    fun withCardId(cardId: Long): KaduoduoViewModelFactory {
        return KaduoduoViewModelFactory(repository, cardId)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(repository) as T

            modelClass.isAssignableFrom(AddCardViewModel::class.java) ->
                AddCardViewModel(repository) as T

            modelClass.isAssignableFrom(CardDetailViewModel::class.java) ->
                CardDetailViewModel(requireNotNull(cardId), repository) as T

            else -> error("未知 ViewModel: ${modelClass.name}")
        }
    }
}

private fun String.toFenOrNull(): Long? {
    val value = trim().takeIf { it.isNotBlank() }?.toBigDecimalOrNull() ?: return null
    return value.movePointRight(2).toLong()
}

private fun String.toLocalDateOrNull(): LocalDate? {
    return runCatching { LocalDate.parse(trim()) }.getOrNull()
}
