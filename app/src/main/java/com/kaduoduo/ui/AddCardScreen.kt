package com.kaduoduo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaduoduo.data.local.FeeConditionType
import com.kaduoduo.data.local.FeeCycleType
import com.kaduoduo.domain.AddCardUiState
import com.kaduoduo.domain.AddCardViewModel

@Composable
fun AddCardScreen(
    viewModel: AddCardViewModel,
    onBackClick: () -> Unit,
    onSaved: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    AddCardContent(
        uiState = uiState,
        onUpdate = viewModel::update,
        onBackClick = onBackClick,
        onSaveClick = { viewModel.save(uiState, onSaved) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddCardContent(
    uiState: AddCardUiState,
    onUpdate: (AddCardUiState.() -> AddCardUiState) -> Unit,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加信用卡") },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionCard(title = "开卡行") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.banks.take(4).forEach { bank ->
                            FilterChip(
                                selected = uiState.selectedBankId == bank.bankId,
                                onClick = {
                                    onUpdate {
                                        copy(selectedBankId = bank.bankId, customBankName = "")
                                    }
                                },
                                label = { Text(bank.name) }
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier.height(96.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(uiState.banks.drop(4), key = { it.bankId }) { bank ->
                            OutlinedButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    onUpdate {
                                        copy(selectedBankId = bank.bankId, customBankName = "")
                                    }
                                }
                            ) {
                                Text(bank.name)
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = uiState.customBankName,
                        onValueChange = { value ->
                            onUpdate {
                                copy(customBankName = value, selectedBankId = null)
                            }
                        },
                        label = { Text("自定义银行名称") },
                        singleLine = true
                    )
                }
            }

            item {
                SectionCard(title = "基础信息") {
                    FormTextField(
                        value = uiState.cardName,
                        onValueChange = { value -> onUpdate { copy(cardName = value) } },
                        label = "卡片名称"
                    )
                    FormTextField(
                        value = uiState.issueDateText,
                        onValueChange = { value -> onUpdate { copy(issueDateText = value) } },
                        label = "发卡日期，格式 2026-06-02"
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FormTextField(
                            modifier = Modifier.weight(1f),
                            value = uiState.billingDay,
                            onValueChange = { value -> onUpdate { copy(billingDay = value) } },
                            label = "账单日",
                            keyboardType = KeyboardType.Number
                        )
                        FormTextField(
                            modifier = Modifier.weight(1f),
                            value = uiState.repaymentDay,
                            onValueChange = { value -> onUpdate { copy(repaymentDay = value) } },
                            label = "还款日",
                            keyboardType = KeyboardType.Number
                        )
                    }
                }
            }

            item {
                SectionCard(title = "额度") {
                    FormTextField(
                        value = uiState.fixedLimitYuan,
                        onValueChange = { value -> onUpdate { copy(fixedLimitYuan = value) } },
                        label = "固定额度，单位元",
                        keyboardType = KeyboardType.Decimal
                    )
                    FormTextField(
                        value = uiState.temporaryLimitYuan,
                        onValueChange = { value -> onUpdate { copy(temporaryLimitYuan = value) } },
                        label = "临时额度，单位元，可不填",
                        keyboardType = KeyboardType.Decimal
                    )
                    FormTextField(
                        value = uiState.temporaryExpireDateText,
                        onValueChange = { value -> onUpdate { copy(temporaryExpireDateText = value) } },
                        label = "临额失效日期，可不填"
                    )
                }
            }

            item {
                SectionCard(title = "年费规则") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = uiState.cycleType == FeeCycleType.NATURAL_YEAR,
                            onClick = { onUpdate { copy(cycleType = FeeCycleType.NATURAL_YEAR) } },
                            label = { Text("自然年") }
                        )
                        FilterChip(
                            selected = uiState.cycleType == FeeCycleType.ISSUE_DATE_YEAR,
                            onClick = { onUpdate { copy(cycleType = FeeCycleType.ISSUE_DATE_YEAR) } },
                            label = { Text("核卡日") }
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = uiState.conditionType == FeeConditionType.SPEND_COUNT,
                            onClick = { onUpdate { copy(conditionType = FeeConditionType.SPEND_COUNT) } },
                            label = { Text("按笔数") }
                        )
                        FilterChip(
                            selected = uiState.conditionType == FeeConditionType.SPEND_AMOUNT,
                            onClick = { onUpdate { copy(conditionType = FeeConditionType.SPEND_AMOUNT) } },
                            label = { Text("按金额") }
                        )
                        FilterChip(
                            selected = uiState.conditionType == FeeConditionType.BOTH,
                            onClick = { onUpdate { copy(conditionType = FeeConditionType.BOTH) } },
                            label = { Text("都需要") }
                        )
                    }
                    FormTextField(
                        value = uiState.requiredCount,
                        onValueChange = { value -> onUpdate { copy(requiredCount = value) } },
                        label = "免年费所需笔数",
                        keyboardType = KeyboardType.Number
                    )
                    FormTextField(
                        value = uiState.requiredAmountYuan,
                        onValueChange = { value -> onUpdate { copy(requiredAmountYuan = value) } },
                        label = "免年费所需金额，单位元",
                        keyboardType = KeyboardType.Decimal
                    )
                }
            }

            uiState.errorMessage?.let { message ->
                item {
                    Text(message, color = MaterialTheme.colorScheme.error)
                }
            }

            item {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onSaveClick
                ) {
                    Text("保存")
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        modifier = modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true
    )
}
