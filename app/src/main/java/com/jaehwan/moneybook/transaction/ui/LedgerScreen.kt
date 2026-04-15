package com.jaehwan.moneybook.transaction.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jaehwan.moneybook.splitmember.data.local.SplitMemberEntity
import com.jaehwan.moneybook.transaction.data.local.TransactionEntity
import com.jaehwan.moneybook.transaction.domain.model.TransactionType
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.TemporalAdjusters
import java.util.Locale

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

    val now = LocalDate.now()
    val startOfMonth = now.withDayOfMonth(1)
    val endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth())
    val monthlyRows = rows.filter { row ->
        val d = Instant.ofEpochMilli(row.transaction.expectedDate)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        !d.isBefore(startOfMonth) && !d.isAfter(endOfMonth)
    }
    val monthlyIncome = monthlyRows.sumOf { row ->
        when (TransactionType.fromKey(row.transaction.type)) {
            TransactionType.INCOME -> row.transaction.amount
            TransactionType.FIXED_INCOME -> if (row.transaction.isConfirmed) row.transaction.amount else 0
            else -> 0
        }
    }
    val monthlyExpense = monthlyRows.sumOf { row ->
        when (TransactionType.fromKey(row.transaction.type)) {
            TransactionType.EXPENSE, TransactionType.SPLIT -> row.transaction.amount
            TransactionType.FIXED_EXPENSE -> if (row.transaction.isConfirmed) row.transaction.amount else 0
            else -> 0
        }
    }
    val monthlyBalance = monthlyIncome - monthlyExpense
    val categoryExpenses = monthlyRows
        .filter {
            val t = TransactionType.fromKey(it.transaction.type)
            t == TransactionType.EXPENSE || t == TransactionType.SPLIT ||
                (t == TransactionType.FIXED_EXPENSE && it.transaction.isConfirmed)
        }
        .groupBy { it.categoryName }
        .mapValues { (_, list) -> list.sumOf { it.transaction.amount } }
        .toList()
        .sortedByDescending { it.second }
        .take(5)
    val recentRows = rows.sortedByDescending { it.transaction.expectedDate }.take(10)
    val monthTitle = "${now.year}년 ${now.month.getDisplayName(TextStyle.FULL, Locale.KOREAN)}"

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            HomeHeader()
        }
        item {
            MonthSelector(monthTitle = monthTitle)
        }
        item {
            MonthlySummaryCard(
                income = monthlyIncome,
                expense = monthlyExpense,
                balance = monthlyBalance,
            )
        }
        item {
            CategoryExpenseCard(items = categoryExpenses)
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "최근 거래",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "최근 ${recentRows.size}건",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        items(items = recentRows, key = { it.transaction.id }) { row ->
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
private fun HomeHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "안녕하세요",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "머니북",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Text(
                text = "알림",
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun MonthSelector(monthTitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
            Text("〈", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = monthTitle,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Surface(shape = MaterialTheme.shapes.small, color = MaterialTheme.colorScheme.surfaceVariant) {
            Text("〉", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
        }
    }
}

@Composable
private fun MonthlySummaryCard(
    income: Int,
    expense: Int,
    balance: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF11C78B)),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("이번 달 잔액", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${formatMoney(balance)}원",
                color = Color.White,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MiniSummaryCard(
                    title = "총 수입",
                    amount = income,
                    positive = true,
                    modifier = Modifier.weight(1f),
                )
                MiniSummaryCard(
                    title = "총 지출",
                    amount = expense,
                    positive = false,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MiniSummaryCard(
    title: String,
    amount: Int,
    positive: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f)),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, color = Color.White, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${if (positive) "+" else "-"}${formatMoney(amount)}원",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun CategoryExpenseCard(items: List<Pair<String, Int>>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("카테고리별 지출", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))
            if (items.isEmpty()) {
                Text(
                    text = "이번 달 지출 데이터가 없습니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                val max = items.maxOf { it.second }.coerceAtLeast(1)
                items.forEach { (name, amount) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(name, style = MaterialTheme.typography.bodyLarge)
                        Text("${formatMoney(amount)}원", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    val ratio = amount.toFloat() / max.toFloat()
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(ratio)
                                .height(8.dp)
                                .background(Color(0xFF6E7CF8), shape = MaterialTheme.shapes.small)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
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
    val incomeLike = isIncomeLike(type)
    val amountColor = if (incomeLike) Color(0xFF00B874) else Color(0xFFFF6363)
    val signedAmount = "${if (incomeLike) "+" else "-"}${formatMoney(tx.amount)}원"

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = row.categoryName,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = signedAmount,
                            style = MaterialTheme.typography.titleMedium,
                            color = amountColor,
                            fontWeight = FontWeight.Bold,
                        )
                    }
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
                                val amountLabel = formatMoney(m.agreedAmount ?: 0)
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

private fun formatMoney(amount: Int): String =
    NumberFormat.getNumberInstance(Locale.KOREA).format(amount)

private fun isIncomeLike(type: TransactionType): Boolean =
    when (type) {
        TransactionType.INCOME, TransactionType.FIXED_INCOME -> true
        TransactionType.EXPENSE, TransactionType.SPLIT -> false
        TransactionType.FIXED_EXPENSE -> false
    }
