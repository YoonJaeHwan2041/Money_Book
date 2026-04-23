package com.jaehwan.moneybook.transaction.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.unit.dp
import com.jaehwan.moneybook.transaction.data.local.InstallmentPaymentEntity
import com.jaehwan.moneybook.splitmember.data.local.SplitMemberEntity
import com.jaehwan.moneybook.transaction.data.local.TransactionEntity
import com.jaehwan.moneybook.transaction.domain.model.TransactionType
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradeAllTransactionsScreen(
    rows: List<LedgerRow>,
    installmentSummary: InstallmentSummary,
    onBack: () -> Unit,
    onAddClick: () -> Unit,
    onOpenDetail: (LedgerRow) -> Unit,
    onSplitMemberPaidToggle: (SplitMemberEntity) -> Unit,
    onInstallmentPaidToggle: (InstallmentPaymentEntity) -> Unit,
    onDeleteTransactions: (List<TransactionEntity>) -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedType by rememberSaveable { mutableStateOf(AllTradeTypeTab.All) }
    var selectedCategory by rememberSaveable { mutableStateOf<String?>(null) }
    var showQuickDeleteIntro by remember { mutableStateOf(false) }
    var selectionMode by rememberSaveable { mutableStateOf(false) }
    var selectedIds by rememberSaveable { mutableStateOf(setOf<Long>()) }

    val availableCategories = rows.map { it.categoryName }.distinct().sorted()
    val globalBalance by remember(rows) {
        derivedStateOf { calculateCurrentBalance(rows) }
    }
    val filteredRows = rows
        .filter { row -> selectedCategory == null || row.categoryName == selectedCategory }
        .filter { row ->
            when (selectedType) {
                AllTradeTypeTab.All -> true
                AllTradeTypeTab.Income -> isIncomeTypeKey(row.transaction.type)
                AllTradeTypeTab.Expense -> !isIncomeTypeKey(row.transaction.type)
                AllTradeTypeTab.Installment -> TransactionType.fromKey(row.transaction.type) == TransactionType.INSTALLMENT
            }
        }
        .filter { row -> matchesSearchAll(row, query) }
        .sortedByDescending { it.transaction.expectedDate }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("전체 거래") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("월별 보기")
                    }
                },
                actions = {
                    if (!selectionMode) {
                        TextButton(onClick = { showQuickDeleteIntro = true }) {
                            Text("간편 삭제")
                        }
                    }
                },
            )
        },
        bottomBar = {
            if (selectionMode) {
                Surface(tonalElevation = 3.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TextButton(
                            onClick = {
                                selectionMode = false
                                selectedIds = emptySet()
                            },
                        ) {
                            Text("취소")
                        }
                        Text(
                            text = "${selectedIds.size}개 선택",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        TextButton(
                            onClick = {
                                val toDelete = rows
                                    .filter { it.transaction.id in selectedIds }
                                    .map { it.transaction }
                                if (toDelete.isNotEmpty()) {
                                    onDeleteTransactions(toDelete)
                                }
                                selectionMode = false
                                selectedIds = emptySet()
                            },
                            enabled = selectedIds.isNotEmpty(),
                        ) {
                            Text("삭제 확인")
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
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
                    if (!selectionMode) {
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
            }
            item {
                AllTradeSummaryCard(
                    balance = globalBalance,
                    installmentSummary = installmentSummary,
                )
            }
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !selectionMode,
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
                    AllFilterChip(
                        label = "전체",
                        selected = selectedCategory == null,
                        onClick = { if (!selectionMode) selectedCategory = null },
                    )
                    availableCategories.forEach { category ->
                        AllFilterChip(
                            label = category,
                            selected = selectedCategory == category,
                            onClick = { if (!selectionMode) selectedCategory = category },
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
                        AllTradeTypeTab.entries.forEach { tab ->
                            val selected = selectedType == tab
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable(enabled = !selectionMode) { selectedType = tab },
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
                        text = if (rows.isEmpty()) "거래 데이터가 없습니다." else "검색 결과가 없습니다.",
                        modifier = Modifier.padding(top = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(items = filteredRows, key = { it.transaction.id }) { row ->
                    val id = row.transaction.id
                    TradeTransactionRow(
                        row = row,
                        onOpenDetail = { onOpenDetail(row) },
                        onSplitMemberPaidToggle = onSplitMemberPaidToggle,
                        onInstallmentPaidToggle = onInstallmentPaidToggle,
                        selectionMode = selectionMode,
                        selected = id in selectedIds,
                        onToggleSelect = {
                            selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
                        },
                    )
                }
            }
        }
    }

    if (showQuickDeleteIntro) {
        AlertDialog(
            onDismissRequest = { showQuickDeleteIntro = false },
            title = { Text("간편 삭제") },
            text = { Text("간편 삭제 기능을 사용하겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showQuickDeleteIntro = false
                        selectionMode = true
                        selectedIds = emptySet()
                    },
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuickDeleteIntro = false }) {
                    Text("취소")
                }
            },
        )
    }
}

@Composable
private fun AllTradeSummaryCard(
    balance: Int,
    installmentSummary: InstallmentSummary,
) {
    var showInstallment by rememberSaveable { mutableStateOf(true) }
    var showMode by rememberSaveable { mutableStateOf(AllSummaryMode.WithInstallment) }
    val adjustedBalance = balance - installmentSummary.remainingTotal
    val mainBalance = if (showMode == AllSummaryMode.WithInstallment) adjustedBalance else balance
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF11C78B)),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = if (showMode == AllSummaryMode.WithInstallment) "할부금 포함 금액" else "지금 잔고",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
            )
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(4.dp))
            Text(
                text = "${if (mainBalance >= 0) "+" else "-"}${com.jaehwan.moneybook.transaction.domain.formatMoney(kotlin.math.abs(mainBalance))}원",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            if (showInstallment) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(2.dp))
                Text(
                    text = "-${com.jaehwan.moneybook.transaction.domain.formatMoney(installmentSummary.remainingTotal)}원 · 할부 ${installmentSummary.activeCount}건",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFFFD6D6),
                )
            }
            androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AllSummaryMode.entries.forEach { mode ->
                        AllModeChip(
                            label = mode.label,
                            selected = showMode == mode,
                            onClick = { showMode = mode },
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("할부 표시", color = Color.White, style = MaterialTheme.typography.labelSmall)
                    Switch(checked = showInstallment, onCheckedChange = { showInstallment = it })
                }
            }
        }
    }
}

private enum class AllTradeTypeTab(val label: String) {
    All("전체"),
    Income("수입"),
    Expense("지출"),
    Installment("할부"),
}

private enum class AllSummaryMode(val label: String) {
    WithInstallment("할부금 포함 금액"),
    RawBalance("지금 잔고"),
}

@Composable
private fun AllModeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (selected) Color.White else Color.White.copy(alpha = 0.18f),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color(0xFF0AA870) else Color.White,
        )
    }
}

@Composable
private fun AllFilterChip(
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

private fun isIncomeTypeKey(typeKey: String): Boolean =
    when (TransactionType.fromKey(typeKey)) {
        TransactionType.INCOME, TransactionType.FIXED_INCOME -> true
        TransactionType.EXPENSE,
        TransactionType.INSTALLMENT,
        TransactionType.SPLIT,
        TransactionType.FIXED_EXPENSE -> false
    }

private fun matchesSearchAll(row: LedgerRow, query: String): Boolean {
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
