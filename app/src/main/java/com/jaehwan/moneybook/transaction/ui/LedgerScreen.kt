package com.jaehwan.moneybook.transaction.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.jaehwan.moneybook.splitmember.data.local.SplitMemberEntity
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
    onSplitMemberPaidToggle: (SplitMemberEntity) -> Unit,
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
                onDeleteRequest = { onDeleteRequest(row.transaction) },
                onSplitMemberPaidToggle = onSplitMemberPaidToggle,
            )
        }
    }
}

@Composable
private fun LedgerTransactionCard(
    row: LedgerRow,
    onEdit: () -> Unit,
    onDeleteRequest: () -> Unit,
    onSplitMemberPaidToggle: (SplitMemberEntity) -> Unit,
) {
    val tx = row.transaction
    val type = TransactionType.fromKey(tx.type)
    val dateStr = formatExpectedDate(tx.expectedDate)
    val pendingFixed = type.isFixed && !tx.isConfirmed
    val modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
        .then(if (pendingFixed) Modifier.alpha(0.72f) else Modifier)

    var splitExpanded by rememberSaveable(tx.id) { mutableStateOf(false) }
    val isSplit = type == TransactionType.SPLIT
    val members = row.splitMembers
    val paidCount = members.count { it.isPaid }
    val memberTotal = members.size

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
                        text = if (isSplit) "뿜빠이" else type.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${tx.amount}원 · ${row.categoryName}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (isSplit && memberTotal > 0) {
                        Text(
                            text = "수금 $paidCount / $memberTotal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
            if (isSplit) {
                TextButton(onClick = { splitExpanded = !splitExpanded }) {
                    Text(if (splitExpanded) "정산 접기" else "정산 펼치기")
                }
                AnimatedVisibility(visible = splitExpanded) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        if (members.isEmpty()) {
                            Text(
                                text = "멤버 정보가 없습니다. 수정에서 다시 저장해 주세요.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            members.forEach { m ->
                                val label = if (m.isPrimaryPayer) "${m.memberName} (결제)" else m.memberName
                                val amountLabel = (m.agreedAmount ?: 0).toString()
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = label, style = MaterialTheme.typography.bodyMedium)
                                        Text(
                                            text = "${amountLabel}원",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (m.isPaid) "받음" else "미수",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                        Switch(
                                            checked = m.isPaid,
                                            onCheckedChange = {
                                                onSplitMemberPaidToggle(
                                                    m.copy(isPaid = it, updatedAt = System.currentTimeMillis())
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                        }
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
