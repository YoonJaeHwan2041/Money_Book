package com.jaehwan.moneybook.transaction.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.jaehwan.moneybook.transaction.data.local.TransactionEntity
import com.jaehwan.moneybook.transaction.domain.model.TransactionType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun LedgerScreen(
    rows: List<LedgerRow>,
    categoriesEmpty: Boolean,
    onEdit: (TransactionEntity) -> Unit,
    onDeleteRequest: (TransactionEntity) -> Unit,
) {
    if (categoriesEmpty) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "카테고리를 먼저 추가해 주세요.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }
    if (rows.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "등록된 거래가 없습니다.",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "+ 버튼으로 거래를 추가할 수 있습니다.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(items = rows, key = { it.transaction.id }) { row ->
            LedgerTransactionCard(
                row = row,
                onEdit = { onEdit(row.transaction) },
                onDeleteRequest = { onDeleteRequest(row.transaction) }
            )
        }
    }
}

@Composable
private fun LedgerTransactionCard(
    row: LedgerRow,
    onEdit: () -> Unit,
    onDeleteRequest: () -> Unit,
) {
    val tx = row.transaction
    val type = TransactionType.fromKey(tx.type)
    val dateStr = formatExpectedDate(tx.expectedDate)
    val pendingFixed = type.isFixed && !tx.isConfirmed
    val modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .then(if (pendingFixed) Modifier.alpha(0.72f) else Modifier)

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (pendingFixed) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = type.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${tx.amount}원 · ${row.categoryName}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (tx.memo != null && tx.memo.isNotBlank()) {
                        Text(
                            text = tx.memo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (type.isFixed) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "예정일 $dateStr · 알람 ${if (tx.hasAlarm) "켜짐" else "꺼짐"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (pendingFixed) {
                            Text(
                                text = "예정 · 미확정",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        } else {
                            Text(
                                text = "확정됨",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    TextButton(onClick = onEdit) {
                        Text("수정")
                    }
                    TextButton(onClick = onDeleteRequest) {
                        Text("삭제")
                    }
                }
            }
        }
    }
}

private fun formatExpectedDate(epochMillis: Long): String {
    val z = ZoneId.systemDefault()
    val d = Instant.ofEpochMilli(epochMillis).atZone(z).toLocalDate()
    return DateTimeFormatter.ISO_LOCAL_DATE.format(d)
}
