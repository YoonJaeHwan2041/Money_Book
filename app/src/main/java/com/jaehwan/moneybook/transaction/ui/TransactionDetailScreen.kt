package com.jaehwan.moneybook.transaction.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jaehwan.moneybook.transaction.data.local.InstallmentPaymentEntity
import com.jaehwan.moneybook.transaction.data.local.TransactionEntity
import com.jaehwan.moneybook.splitmember.data.local.SplitMemberEntity
import com.jaehwan.moneybook.transaction.domain.model.TransactionType
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailScreen(
    row: LedgerRow,
    onBack: () -> Unit,
    onEdit: (TransactionEntity) -> Unit,
    onDeleteRequest: (TransactionEntity) -> Unit,
    onSplitMemberPaidToggle: (SplitMemberEntity) -> Unit,
    onInstallmentPaidToggle: (InstallmentPaymentEntity) -> Unit,
) {
    val tx = row.transaction
    val type = TransactionType.fromKey(tx.type)
    val splitMembers = row.splitMembers
    val installmentPlan = row.installmentPlan
    val installmentPayments = row.installmentPayments
    val isSplit = type == TransactionType.SPLIT
    val incomeLike = type == TransactionType.INCOME || type == TransactionType.FIXED_INCOME
    val unpaidTotal = splitMembers
        .let { com.jaehwan.moneybook.transaction.domain.unpaidTotal(it) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("거래 상세") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("뒤로")
                    }
                },
                actions = {
                    TextButton(onClick = { onEdit(row.transaction) }) {
                        Text("수정")
                    }
                    TextButton(onClick = { onDeleteRequest(row.transaction) }) {
                        Text("삭제")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "${if (incomeLike) "+" else "-"}${formatMoney(tx.amount)}원",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (incomeLike) Color(0xFF00B874) else Color(0xFFFF6363),
                    )
                    HorizontalDivider()
                    DetailLine(label = "유형", value = type.label)
                    DetailLine(label = "카테고리", value = row.categoryName)
                    DetailLine(label = "날짜", value = formatDate(tx.expectedDate))
                    if (type.isFixed) {
                        DetailLine(
                            label = "고정거래",
                            value = "${if (tx.isConfirmed) "확정" else "미확정"} · 알람 ${if (tx.hasAlarm) "켜짐" else "꺼짐"}",
                        )
                    }
                }
            }
            item {
                HorizontalDivider()
            }
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = "메모",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = tx.memo?.takeIf { it.isNotBlank() } ?: "메모 없음",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            if (isSplit) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSplitComplete(splitMembers)) Color(0xFFE9FFF3) else Color(0xFFF4EEFF)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("뿜빠이 멤버", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "진행 상태: ${splitMembers.count { it.isPaid }} / ${splitMembers.size}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                "미정산 금액 합계: ${formatMoney(unpaidTotal)}원",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                items(splitMembers, key = { it.id }) { member ->
                    Card {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column {
                                    Text(
                                        text = if (member.isPrimaryPayer) "${member.memberName} (결제자)" else member.memberName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        text = "합의금 ${formatMoney(member.agreedAmount ?: 0)}원",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (member.isPrimaryPayer) {
                                        Text("본인 부담 완료")
                                    } else {
                                        Text(if (member.isPaid) "정산완료" else "미정산")
                                        Switch(
                                            checked = member.isPaid,
                                            onCheckedChange = {
                                                onSplitMemberPaidToggle(
                                                    member.copy(isPaid = it, updatedAt = System.currentTimeMillis())
                                                )
                                            }
                                        )
                                    }
                                }
                            }
                            HorizontalDivider()
                            Text("추가금 ${formatMoney(member.extraAmount ?: 0)}원 / 차감 ${formatMoney(member.deductionAmount ?: 0)}원")
                            Text("메모: ${member.paymentMemo?.takeIf { it.isNotBlank() } ?: "없음"}")
                        }
                    }
                }
            }
            if (installmentPlan != null) {
                item {
                    HorizontalDivider()
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF1F1))
                    ) {
                        val paidCount = installmentPayments.count { it.isPaid }
                        val remaining = installmentPayments.filterNot { it.isPaid }.sumOf { it.amount }
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("할부 정보", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("총 할부금: ${formatMoney(installmentPlan.totalAmount)}원")
                            Text("진행 회차: $paidCount / ${installmentPlan.months}")
                            Text("남은 원금: ${formatMoney(remaining)}원")
                        }
                    }
                }
                items(installmentPayments, key = { it.id }) { payment ->
                    Card {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "${payment.sequenceNo}회차 · ${formatDate(payment.dueDate)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Text(
                                    text = "${formatMoney(payment.amount)}원",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (payment.isPaid) "납부완료" else "미납")
                                Switch(
                                    checked = payment.isPaid,
                                    onCheckedChange = { checked ->
                                        onInstallmentPaidToggle(
                                            payment.copy(
                                                isPaid = checked,
                                                paidAt = if (checked) System.currentTimeMillis() else null,
                                            )
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

@Composable
private fun DetailLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatDate(epochMillis: Long): String =
    DateTimeFormatter.ISO_LOCAL_DATE.format(
        Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    )

private fun formatMoney(amount: Int): String =
    com.jaehwan.moneybook.transaction.domain.formatMoney(amount)

private fun isSplitComplete(members: List<SplitMemberEntity>): Boolean {
    return com.jaehwan.moneybook.transaction.domain.isSplitComplete(members)
}
