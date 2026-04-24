package com.jaehwan.moneybook.transaction.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jaehwan.moneybook.category.ui.CategoryIconDisplay
import com.jaehwan.moneybook.splitmember.data.local.SplitMemberEntity
import com.jaehwan.moneybook.transaction.data.local.TransactionEntity
import com.jaehwan.moneybook.transaction.domain.model.TransactionType
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun LedgerScreen(
    rows: List<LedgerRow>,
    installmentSummary: InstallmentSummary,
    categoriesEmpty: Boolean,
    onEdit: (TransactionEntity) -> Unit,
    onDeleteRequest: (TransactionEntity) -> Unit,
    onConfirmPendingFixed: (LedgerRow) -> Unit = {},
    onDiscardPendingFixed: (LedgerRow) -> Unit = {},
    onSplitMemberPaidToggle: (SplitMemberEntity) -> Unit,
    onOpenDetail: (LedgerRow) -> Unit,
    showActionButtons: Boolean = false,
    allowInlineSplitExpand: Boolean = false,
    onViewAll: () -> Unit = {},
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

    val pivotEpoch = rows.maxOfOrNull { it.transaction.expectedDate } ?: System.currentTimeMillis()
    val pivotDate = Instant.ofEpochMilli(pivotEpoch).atZone(ZoneId.systemDefault()).toLocalDate()
    var selectedMonth by remember {
        mutableStateOf(YearMonth.of(pivotDate.year, pivotDate.month))
    }
    var showMonthPicker by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(rows) {
        if (rows.none { it.isInMonth(selectedMonth) }) {
            val latest = rows.maxOfOrNull { it.transaction.expectedDate }
            if (latest != null) {
                val d = Instant.ofEpochMilli(latest).atZone(ZoneId.systemDefault()).toLocalDate()
                selectedMonth = YearMonth.of(d.year, d.monthValue)
            }
        }
    }
    val monthlyRows by remember(rows, selectedMonth) {
        derivedStateOf { rows.filter { it.isInMonth(selectedMonth) } }
    }
    val monthlyTotals by remember(monthlyRows) {
        derivedStateOf { calculateMonthlyTotals(monthlyRows) }
    }
    val monthlyIncome = monthlyTotals.income
    val monthlyExpense = monthlyTotals.expense
    val monthlyInstallment by remember(monthlyRows) {
        derivedStateOf {
            monthlyRows
                .filter { TransactionType.fromKey(it.transaction.type) == TransactionType.INSTALLMENT }
                .sumOf { it.transaction.amount }
        }
    }
    val currentBalance = calculateCurrentBalance(rows)
    val categoryExpenses by remember(monthlyRows) {
        derivedStateOf {
            monthlyRows
                .asSequence()
                .filter {
                    val t = TransactionType.fromKey(it.transaction.type)
                    t == TransactionType.EXPENSE || t == TransactionType.INSTALLMENT || t == TransactionType.SPLIT ||
                        (t == TransactionType.FIXED_EXPENSE && it.transaction.isConfirmed)
                }
                .groupBy { Triple(it.transaction.categoryId, it.categoryName, it.categoryIconKey) }
                .map { (key, list) ->
                    CategoryExpenseItem(
                        categoryName = key.second,
                        categoryIconKey = key.third,
                        amount = list.sumOf { it.transaction.amount },
                    )
                }
                .sortedByDescending { it.amount }
                .take(5)
        }
    }
    var recentFilter by rememberSaveable { mutableStateOf(RecentFilter.All) }
    val recentRows by remember(monthlyRows, recentFilter) {
        derivedStateOf {
            monthlyRows
                .asSequence()
                .filter {
                    when (recentFilter) {
                        RecentFilter.All -> true
                        RecentFilter.Income -> isIncomeLike(TransactionType.fromKey(it.transaction.type))
                        RecentFilter.Expense -> !isIncomeLike(TransactionType.fromKey(it.transaction.type))
                        RecentFilter.Installment -> TransactionType.fromKey(it.transaction.type) == TransactionType.INSTALLMENT
                    }
                }
                .sortedByDescending { it.transaction.expectedDate }
                .take(10)
                .toList()
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            HomeHeader()
        }
        item {
            MonthSelectorBar(
                selectedMonth = selectedMonth,
                onChangeMonth = { selectedMonth = it },
                onOpenPicker = { showMonthPicker = true },
            )
        }
        item {
            MonthlySummaryCard(
                income = monthlyIncome,
                expense = monthlyExpense,
                installment = monthlyInstallment,
                balance = currentBalance,
                installmentSummary = installmentSummary,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "최근 ${recentRows.size}건",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onViewAll) {
                        Text("전체보기")
                    }
                }
            }
        }
        item {
            RecentFilterTabs(
                selected = recentFilter,
                onSelect = { recentFilter = it },
            )
        }
        items(items = recentRows, key = { it.transaction.id }) { row ->
            if (showActionButtons) {
                LedgerTransactionCard(
                    row = row,
                    onEdit = { onEdit(row.transaction) },
                    onDeleteRequest = { onDeleteRequest(row.transaction) },
                    onConfirmPendingFixed = { onConfirmPendingFixed(row) },
                    onDiscardPendingFixed = { onDiscardPendingFixed(row) },
                    onSplitMemberPaidToggle = onSplitMemberPaidToggle,
                    onOpenDetail = { onOpenDetail(row) },
                    showActionButtons = true,
                    allowInlineSplitExpand = allowInlineSplitExpand,
                )
            } else {
                TradeTransactionRow(
                    row = row,
                    onOpenDetail = { onOpenDetail(row) },
                    onConfirmPendingFixed = { onConfirmPendingFixed(row) },
                    onDiscardPendingFixed = { onDiscardPendingFixed(row) },
                    onSplitMemberPaidToggle = onSplitMemberPaidToggle,
                )
            }
        }
    }

    if (showMonthPicker) {
        YearMonthPickerDialog(
            initial = selectedMonth,
            onDismiss = { showMonthPicker = false },
            onConfirm = {
                selectedMonth = it
                showMonthPicker = false
            },
        )
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
private fun MonthlySummaryCard(
    income: Int,
    expense: Int,
    installment: Int,
    balance: Int,
    installmentSummary: InstallmentSummary,
) {
    TransactionOverviewCard(
        balance = balance,
        income = income,
        expense = expense,
        installment = installment,
        installmentSummary = installmentSummary,
        rawBalanceTitle = "현재 통장 잔고",
    )
}

@Composable
private fun CategoryExpenseCard(items: List<CategoryExpenseItem>) {
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
                val max = items.maxOf { it.amount }.coerceAtLeast(1)
                items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CategoryIconDisplay(
                                iconKey = item.categoryIconKey,
                                modifier = Modifier.size(20.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(item.categoryName, style = MaterialTheme.typography.bodyLarge)
                        }
                        Text(
                            "${formatMoney(item.amount)}원",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    val ratio = item.amount.toFloat() / max.toFloat()
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
private fun RecentFilterTabs(
    selected: RecentFilter,
    onSelect: (RecentFilter) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        RecentFilter.entries.forEach { filter ->
            val active = filter == selected
            Surface(
                shape = CircleShape,
                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable { onSelect(filter) },
            ) {
                Text(
                    text = filter.label,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LedgerTransactionCard(
    row: LedgerRow,
    onEdit: () -> Unit,
    onDeleteRequest: () -> Unit,
    onConfirmPendingFixed: () -> Unit,
    onDiscardPendingFixed: () -> Unit,
    onSplitMemberPaidToggle: (SplitMemberEntity) -> Unit,
    onOpenDetail: () -> Unit,
    showActionButtons: Boolean,
    allowInlineSplitExpand: Boolean,
) {
    val tx = row.transaction
    val type = TransactionType.fromKey(tx.type)
    val dateStr = formatExpectedDate(tx.expectedDate)
    val pendingFixed = type.isFixed && !tx.isConfirmed
    var showPendingConfirmDialog by rememberSaveable(tx.id) { mutableStateOf(false) }
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
    val hasMemo = !tx.memo.isNullOrBlank()
    val splitDone = isSplitComplete(members)
    val containerColor = when {
        pendingFixed -> Color(0xFFEAF4FF)
        isSplit && splitDone -> Color(0xFFE9FFF3)
        isSplit -> Color(0xFFF4EEFF)
        hasMemo -> Color(0xFFFFF9DB)
        pendingFixed -> MaterialTheme.colorScheme.surfaceVariant
        else -> MaterialTheme.colorScheme.surface
    }

    Card(
        modifier = modifier.clickable {
            if (pendingFixed) showPendingConfirmDialog = true else onOpenDetail()
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CategoryIconDisplay(
                        iconKey = row.categoryIconKey,
                        modifier = Modifier.width(52.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tx.memo?.takeIf { it.isNotBlank() } ?: if (isSplit) "뿜빠이 정산" else row.categoryName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = "${row.categoryName} · ${formatMonthDay(tx.expectedDate)}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = signedAmount,
                            style = MaterialTheme.typography.titleMedium,
                            color = amountColor,
                            fontWeight = FontWeight.Bold,
                        )
                        if (isSplit && memberTotal > 0) {
                            Text(
                                text = "수금 $paidCount / $memberTotal",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (showActionButtons) {
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
            if (isSplit && allowInlineSplitExpand) {
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
                                        if (m.isPrimaryPayer) {
                                            Text(
                                                text = "본인 부담 완료",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        } else {
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
    if (showPendingConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showPendingConfirmDialog = false },
            title = { Text("고정거래 적용") },
            text = { Text("해당 금액을 적용하겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPendingConfirmDialog = false
                        onConfirmPendingFixed()
                    }
                ) { Text("Yes") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPendingConfirmDialog = false
                        onDiscardPendingFixed()
                    }
                ) { Text("No") }
            },
        )
    }
}

private fun formatExpectedDate(epochMillis: Long): String {
    val z = ZoneId.systemDefault()
    val d = Instant.ofEpochMilli(epochMillis).atZone(z).toLocalDate()
    return DateTimeFormatter.ISO_LOCAL_DATE.format(d)
}

private fun formatMoney(amount: Int): String =
    com.jaehwan.moneybook.transaction.domain.formatMoney(amount)

private fun formatMonthDay(epochMillis: Long): String {
    val d = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    return "%02d-%02d".format(d.monthValue, d.dayOfMonth)
}

private fun isIncomeLike(type: TransactionType): Boolean =
    when (type) {
        TransactionType.INCOME, TransactionType.FIXED_INCOME -> true
        TransactionType.EXPENSE, TransactionType.INSTALLMENT, TransactionType.SPLIT -> false
        TransactionType.FIXED_EXPENSE -> false
    }

private fun isSplitComplete(members: List<SplitMemberEntity>): Boolean {
    return com.jaehwan.moneybook.transaction.domain.isSplitComplete(members)
}

private data class CategoryExpenseItem(
    val categoryName: String,
    val categoryIconKey: String?,
    val amount: Int,
)

private enum class RecentFilter(val label: String) {
    All("전체"),
    Income("수입"),
    Expense("지출"),
    Installment("할부"),
}

