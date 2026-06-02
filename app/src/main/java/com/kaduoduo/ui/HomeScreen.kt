package com.kaduoduo.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kaduoduo.data.local.CardOverview
import com.kaduoduo.domain.HomeViewModel

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    onAddClick: () -> Unit,
    onCardClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        cards = uiState.cards,
        isEmpty = uiState.isEmpty,
        onAddClick = onAddClick,
        onCardClick = onCardClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    cards: List<CardOverview>,
    isEmpty: Boolean,
    onAddClick: () -> Unit,
    onCardClick: (Long) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("卡多多") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Text("+")
            }
        }
    ) { padding ->
        if (isEmpty) {
            EmptyCardsState(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                onAddClick = onAddClick
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cards, key = { it.card.cardId }) { item ->
                    CreditCardOverviewCard(
                        item = item,
                        onClick = { onCardClick(item.card.cardId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyCardsState(
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("还没有信用卡", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text("添加第一张卡后，可以跟踪年费、额度和权益。")
            Spacer(Modifier.height(20.dp))
            Button(onClick = onAddClick) {
                Text("添加信用卡")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreditCardOverviewCard(
    item: CardOverview,
    onClick: () -> Unit
) {
    val card = item.card
    val bankName = item.bank?.name ?: card.customBankName ?: "自定义银行"
    val feeProgress = item.feeRule?.annualFeeProgress() ?: 0f

    Card(onClick = onClick) {
        Column(Modifier.padding(16.dp)) {
            Text("$bankName · ${card.cardName}", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Text("固定额度：${card.fixedLimitFen.toYuanText()}")
            card.temporaryLimitFen?.let {
                Text("临时额度：${it.toYuanText()}")
            }
            Spacer(Modifier.height(12.dp))
            Text("年费达标进度 ${(feeProgress * 100).toInt()}%")
            LinearProgressIndicator(
                progress = { feeProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
            )
        }
    }
}
