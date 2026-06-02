package com.kaduoduo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaduoduo.data.local.BenefitEntity
import com.kaduoduo.data.local.CardDetail
import com.kaduoduo.data.local.LimitHistoryEntity
import com.kaduoduo.data.local.LimitType
import com.kaduoduo.domain.CardDetailViewModel

@Composable
fun CardDetailRoute(
    viewModel: CardDetailViewModel,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    CardDetailScreen(
        detail = uiState.detail,
        isLoading = uiState.isLoading,
        onBackClick = onBackClick,
        onRedeemBenefit = viewModel::redeemBenefit,
        onAddFixedLimit = viewModel::addFixedLimitHistory,
        onAddTemporaryLimit = viewModel::addTemporaryLimitHistory,
        onAddBenefit = viewModel::addBenefit
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardDetailScreen(
    detail: CardDetail?,
    isLoading: Boolean,
    onBackClick: () -> Unit,
    onRedeemBenefit: (Long) -> Unit,
    onAddFixedLimit: (String) -> Unit,
    onAddTemporaryLimit: (String, String) -> Unit,
    onAddBenefit: (String, String, String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("卡片详情") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("返回")
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            detail == null -> {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text("没有找到这张卡片")
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { DetailHeader(detail) }

                    item {
                        Text("调额记录", style = MaterialTheme.typography.titleMedium)
                    }

                    item {
                        AddLimitHistoryCard(
                            onAddFixedLimit = onAddFixedLimit,
                            onAddTemporaryLimit = onAddTemporaryLimit
                        )
                    }

                    if (detail.limitHistories.isEmpty()) {
                        item { Text("暂无调额记录") }
                    } else {
                        items(detail.limitHistories, key = { it.historyId }) { history ->
                            LimitHistoryRow(history)
                        }
                    }

                    item {
                        Text("权益管理", style = MaterialTheme.typography.titleMedium)
                    }

                    item {
                        AddBenefitCard(onAddBenefit = onAddBenefit)
                    }

                    if (detail.benefits.isEmpty()) {
                        item { Text("暂无权益记录") }
                    } else {
                        items(detail.benefits, key = { it.benefitId }) { benefit ->
                            BenefitCard(
                                benefit = benefit,
                                onRedeemClick = { onRedeemBenefit(benefit.benefitId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailHeader(detail: CardDetail) {
    val card = detail.card
    val bankName = detail.bank?.name ?: card.customBankName ?: "自定义银行"
    val feeProgress = detail.feeRule?.annualFeeProgress() ?: 0f

    ElevatedCard {
        Column(Modifier.padding(16.dp)) {
            Text("$bankName · ${card.cardName}", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text("账单日：每月 ${card.billingDay} 日")
            Text("还款日：每月 ${card.repaymentDay} 日")
            Text("固定额度：${card.fixedLimitFen.toYuanText()}")
            card.temporaryLimitFen?.let { temporaryLimit ->
                Text("临时额度：${temporaryLimit.toYuanText()}")
            }
            Spacer(Modifier.height(12.dp))
            Text("年费达标 ${(feeProgress * 100).toInt()}%")
            LinearProgressIndicator(
                progress = { feeProgress },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AddLimitHistoryCard(
    onAddFixedLimit: (String) -> Unit,
    onAddTemporaryLimit: (String, String) -> Unit
) {
    var fixedLimit by remember { mutableStateOf("") }
    var temporaryLimit by remember { mutableStateOf("") }
    var temporaryExpire by remember { mutableStateOf("") }

    ElevatedCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("新增调额记录", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = fixedLimit,
                onValueChange = { fixedLimit = it },
                label = { Text("新的固定额度，单位元") },
                singleLine = true
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onAddFixedLimit(fixedLimit)
                    fixedLimit = ""
                }
            ) {
                Text("记录固定额度调整")
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = temporaryLimit,
                onValueChange = { temporaryLimit = it },
                label = { Text("新的临时额度，单位元") },
                singleLine = true
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = temporaryExpire,
                onValueChange = { temporaryExpire = it },
                label = { Text("临额失效日期，格式 2026-06-02") },
                singleLine = true
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onAddTemporaryLimit(temporaryLimit, temporaryExpire)
                    temporaryLimit = ""
                    temporaryExpire = ""
                }
            ) {
                Text("记录临时额度调整")
            }
        }
    }
}

@Composable
private fun LimitHistoryRow(history: LimitHistoryEntity) {
    ElevatedCard {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = if (history.type == LimitType.FIXED) "固定额度调整" else "临时额度调整",
                style = MaterialTheme.typography.titleSmall
            )
            Text("调整日期：${history.adjustDateEpochDay.toDateText()}")
            history.beforeLimitFen?.let { Text("调整前：${it.toYuanText()}") }
            Text("调整后：${history.afterLimitFen.toYuanText()}")
        }
    }
}

@Composable
private fun AddBenefitCard(
    onAddBenefit: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var totalCount by remember { mutableStateOf("") }
    var expireDate by remember { mutableStateOf("") }

    ElevatedCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("新增权益", style = MaterialTheme.typography.titleSmall)
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = name,
                onValueChange = { name = it },
                label = { Text("权益名称，如机场贵宾厅") },
                singleLine = true
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = totalCount,
                onValueChange = { totalCount = it },
                label = { Text("总次数") },
                singleLine = true
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = expireDate,
                onValueChange = { expireDate = it },
                label = { Text("有效期截止日，格式 2026-12-31") },
                singleLine = true
            )
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onAddBenefit(name, totalCount, expireDate)
                    name = ""
                    totalCount = ""
                    expireDate = ""
                }
            ) {
                Text("添加权益")
            }
        }
    }
}

@Composable
private fun BenefitCard(
    benefit: BenefitEntity,
    onRedeemClick: () -> Unit
) {
    ElevatedCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(benefit.name, style = MaterialTheme.typography.titleMedium)
                Text("剩余 ${benefit.remainingCount} / ${benefit.totalCount} 次")
                Text("有效期至：${benefit.expireDateEpochDay.toDateText()}")
            }

            Button(
                onClick = onRedeemClick,
                enabled = benefit.remainingCount > 0
            ) {
                Text("核销")
            }
        }
    }
}
