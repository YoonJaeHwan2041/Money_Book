package com.jaehwan.moneybook.transaction.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jaehwan.moneybook.category.ui.CategoryIconDisplay
import com.jaehwan.moneybook.transaction.data.local.InstallmentPaymentEntity
import com.jaehwan.moneybook.splitmember.data.local.SplitMemberEntity
import com.jaehwan.moneybook.transaction.domain.model.TransactionType
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TradeScreen(
    rows: List<LedgerRow>,
    installmentSummary: InstallmentSummary,
    onAddClick: () -> Unit,
    onOpenDetail: (LedgerRow) -> Unit,
    onSplitMemberPaidToggle: (SplitMemberEntity) -> Unit,
    onInstallmentPaidToggle: (InstallmentPaymentEntity) -> Unit,
    onOpenAllTransactions: () -> Unit,
) {
    if (rows.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("거래 데이터가 없습니다.", style = MaterialTheme.typography.bodyLarge)
            TextButton(onClick = onOpenAllTransactions, modifier = Modifier.padding(top = 12.dp)) {
                Text("전체 거래 보기")
            }
        }
        return
    }

    val pivotEpoch = rows.maxOfOrNull { it.transaction.expectedDate } ?: System.currentTimeMillis()
    val pivotDate = Instant.ofEpochMilli(pivotEpoch).atZone(ZoneId.systemDefault()).toLocalDate()
    var selectedMonth by remember { mutableStateOf(YearMonth.of(pivotDate.year, pivotDate.month)) }
    var showMonthPicker by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var selectedType by rememberSaveable { mutableStateOf(TradeTypeTab.All) }
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }

    val rowsByMonth by remember(rows, selectedMonth) {
        derivedStateOf { rows.filter { it.isInMonth(selectedMonth) } }
    }
    val availableCategories by remember(rowsByMonth) {
        derivedStateOf { rowsByMonth.map { it.categoryName }.distinct().sorted() }
    }
    val filteredRows by remember(rowsByMonth, selectedCategory, selectedType, query) {
        derivedStateOf {
            rowsByMonth
                .asSequence()
                .filter { row -> selectedCategory == null || row.categoryName == selectedCategory }
                .filter { row ->
                    when (selectedType) {
                        TradeTypeTab.All -> true
                        TradeTypeTab.Income -> isIncomeType(row.transaction.type)
                        TradeTypeTab.Expense -> !isIncomeType(row.transaction.type)
                        TradeTypeTab.Installment -> TransactionType.fromKey(row.transaction.type) == TransactionType.INSTALLMENT
                    }
                }
                .filter { row -> matchesSearch(row, query) }
                .sortedByDescending { it.transaction.expectedDate }
                .toList()
        }
    }

    val totalIncome by remember(rowsByMonth) {
        derivedStateOf {
            rowsByMonth.filter { isIncomeType(it.transaction.type) }.sumOf { it.transaction.amount }
        }
    }
    val totalExpense by remember(rowsByMonth) {
        derivedStateOf {
            rowsByMonth.filter { !isIncomeType(it.transaction.type) }.sumOf { it.transaction.amount }
        }
    }
    val totalInstallment by remember(rowsByMonth) {
        derivedStateOf {
            rowsByMonth
                .filter { TransactionType.fromKey(it.transaction.type) == TransactionType.INSTALLMENT }
                .sumOf { it.transaction.amount }
        }
    }
    val globalBalance = calculateCurrentBalance(rows)

    val groupedByDate by remember(filteredRows) {
        derivedStateOf { filteredRows.groupBy { formatDate(it.transaction.expectedDate) } }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("거래 내역", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF11C78B),
                    modifier = Modifier.clickable(onClick = onAddClick),
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "거래 추가",
                        tint = Color.White,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    MonthSelectorBar(
                        selectedMonth = selectedMonth,
                        onChangeMonth = { selectedMonth = it },
                        onOpenPicker = { showMonthPicker = true },
                        monthText = "%04d.%02d".format(selectedMonth.year, selectedMonth.monthValue),
                    )
                }
                TextButton(
                    onClick = onOpenAllTransactions,
                    modifier = Modifier.padding(start = 4.dp),
                ) {
                    Text("전체 거래 보기", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        item {
            TransactionOverviewCard(
                balance = globalBalance,
                income = totalIncome,
                expense = totalExpense,
                installment = totalInstallment,
                installmentSummary = installmentSummary,
                rawBalanceTitle = "지금 잔고",
            )
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "검색") },
                placeholder = { Text("거래 내용 또는 카테고리 검색") },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                ),
            )
        }
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                FilterChip(
                    label = "전체",
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null },
                )
                availableCategories.forEach { category ->
                    FilterChip(
                        label = category,
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                    )
                }
            }
        }
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                shape = RoundedCornerShape(14.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TradeTypeTab.entries.forEach { tab ->
                        val selected = selectedType == tab
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { selectedType = tab },
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = if (selected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                )
                            }
                        }
                    }
                }
            }
        }

        if (filteredRows.isEmpty()) {
            item {
                Text(
                    text = "검색 결과가 없습니다.",
                    modifier = Modifier.padding(top = 12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            groupedByDate.forEach { (date, dateRows) ->
                item {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    ) {
                        Text(
                            text = date,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                items(items = dateRows, key = { it.transaction.id }) { row ->
                    TradeTransactionRow(
                        row = row,
                        onOpenDetail = { onOpenDetail(row) },
                        onSplitMemberPaidToggle = onSplitMemberPaidToggle,
                        onInstallmentPaidToggle = onInstallmentPaidToggle,
                    )
                }
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
private fun FilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun TradeTransactionRow(
    row: LedgerRow,
    onOpenDetail: () -> Unit,
    onSplitMemberPaidToggle: (SplitMemberEntity) -> Unit,
    onInstallmentPaidToggle: (InstallmentPaymentEntity) -> Unit = {},
    selectionMode: Boolean = false,
    selected: Boolean = false,
    onToggleSelect: (() -> Unit)? = null,
) {
    val tx = row.transaction
    val income = isIncomeType(tx.type)
    val type = TransactionType.fromKey(tx.type)
    val isSplit = type == TransactionType.SPLIT
    val hasMemo = !tx.memo.isNullOrBlank()
    var splitExpanded by rememberSaveable(tx.id) { mutableStateOf(false) }
    val members = row.splitMembers
    val hasInstallment = row.installmentPlan != null
    val installmentPaidCount = row.installmentPaidCount
    val installmentRemaining = row.installmentRemainingAmount
    val installmentTotalCount = row.installmentTotalCount
    val splitDone = isSplitComplete(members)
    val baseColor = when {
        hasInstallment -> Color(0xFFFFF1F1)
        isSplit && splitDone -> Color(0xFFE9FFF3)
        isSplit -> Color(0xFFF4EEFF)
        hasMemo -> Color(0xFFFFF9DB)
        else -> MaterialTheme.colorScheme.surface
    }
    val containerColor = when {
        selectionMode && selected -> Color(0xFFFFCDD2)
        else -> baseColor
    }
    val onMainRowClick: () -> Unit = {
        if (selectionMode) onToggleSelect?.invoke() else onOpenDetail()
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onMainRowClick)
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CategoryIconDisplay(iconKey = row.categoryIconKey, modifier = Modifier.size(34.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = tx.memo?.takeIf { it.isNotBlank() } ?: row.categoryName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${row.categoryName} · ${formatDate(tx.expectedDate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .padding(start = 12.dp)
                        .width(126.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        text = "${if (income) "+" else "-"}${formatMoney(tx.amount)}원",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (income) Color(0xFF00B874) else Color(0xFFFF6363),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                    )
                    if (isSplit && !selectionMode) {
                        Icon(
                            imageVector = if (splitExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "스플릿 펼치기",
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .clickable { splitExpanded = !splitExpanded },
                        )
                    }
                }
            }
            if (hasInstallment && !selectionMode) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        text = "할부 진행 ${installmentPaidCount} / ${installmentTotalCount} · 잔액 ${formatMoney(installmentRemaining)}원",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (isSplit && !selectionMode) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))
                AnimatedSplitMembers(
                    expanded = splitExpanded,
                    members = members,
                    onSplitMemberPaidToggle = onSplitMemberPaidToggle,
                )
            }
        }
    }
}

@Composable
private fun AnimatedSplitMembers(
    expanded: Boolean,
    members: List<SplitMemberEntity>,
    onSplitMemberPaidToggle: (SplitMemberEntity) -> Unit,
) {
    androidx.compose.animation.AnimatedVisibility(visible = expanded) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            val unpaidTotal = members
                .let { com.jaehwan.moneybook.transaction.domain.unpaidTotal(it) }
            Text(
                text = "미정산 금액 합계 ${formatMoney(unpaidTotal)}원",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (members.isEmpty()) {
                Text("멤버 정보가 없습니다.", style = MaterialTheme.typography.bodySmall)
            } else {
                members.forEach { member ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                text = if (member.isPrimaryPayer) "${member.memberName} (결제)" else member.memberName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Text(
                                text = "${formatMoney(member.agreedAmount ?: 0)}원",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (member.isPrimaryPayer) {
                                Text("본인 부담 완료")
                            } else {
                                Text(if (member.isPaid) "완료" else "미완료")
                                androidx.compose.material3.Switch(
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
                }
            }
        }
    }
}

private fun isIncomeType(typeKey: String): Boolean =
    when (TransactionType.fromKey(typeKey)) {
        TransactionType.INCOME, TransactionType.FIXED_INCOME -> true
        TransactionType.EXPENSE,
        TransactionType.INSTALLMENT,
        TransactionType.SPLIT,
        TransactionType.FIXED_EXPENSE -> false
    }

private fun matchesSearch(row: LedgerRow, query: String): Boolean {
    val raw = query.trim()
    if (raw.isEmpty()) return true
    val normalized = raw.replace('-', '.').replace('/', '.')
    val date = Instant.ofEpochMilli(row.transaction.expectedDate).atZone(ZoneId.systemDefault()).toLocalDate()
    val yyyyMmDd = "%04d.%02d.%02d".format(date.year, date.monthValue, date.dayOfMonth)
    val yyyyMm = "%04d.%02d".format(date.year, date.monthValue)
    val mm = "%02d".format(date.monthValue)
    val monthNumeric = raw.toIntOrNull()
    val monthMatches = monthNumeric != null && monthNumeric in 1..12 && date.monthValue == monthNumeric
    return row.categoryName.contains(raw, ignoreCase = true) ||
        (row.transaction.memo?.contains(raw, ignoreCase = true) == true) ||
        yyyyMmDd.contains(normalized) ||
        yyyyMm.contains(normalized) ||
        mm == normalized ||
        monthMatches
}

private fun formatDate(epochMillis: Long): String =
    DateTimeFormatter.ISO_LOCAL_DATE.format(
        Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    )

private fun formatMoney(amount: Int): String =
    com.jaehwan.moneybook.transaction.domain.formatMoney(amount)

private enum class TradeTypeTab(val label: String) {
    All("전체"),
    Income("수입"),
    Expense("지출"),
    Installment("할부"),
}

private fun isSplitComplete(members: List<SplitMemberEntity>): Boolean {
    return com.jaehwan.moneybook.transaction.domain.isSplitComplete(members)
}
