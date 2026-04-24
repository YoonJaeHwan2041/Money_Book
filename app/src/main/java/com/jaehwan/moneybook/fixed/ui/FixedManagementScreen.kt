package com.jaehwan.moneybook.fixed.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.jaehwan.moneybook.category.data.local.CategoryEntity
import com.jaehwan.moneybook.category.ui.CategoryIconDisplay
import com.jaehwan.moneybook.fixed.data.local.FixedScheduleEntity
import com.jaehwan.moneybook.transaction.domain.model.TransactionType
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun FixedManagementScreen(
    rows: List<FixedScheduleRow>,
    categories: List<CategoryEntity>,
    monthExpectedExpense: Int,
    monthSpentExpense: Int,
    onAdd: (
        kind: TransactionType,
        categoryId: Long,
        amount: Int,
        memo: String?,
        dayOfMonth: Int,
        triggerHour: Int,
        startYearMonth: String,
    ) -> Unit,
    onUpdate: (FixedScheduleEntity) -> Unit,
    onDelete: (FixedScheduleEntity) -> Unit,
) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var editingSchedule by remember { mutableStateOf<FixedScheduleEntity?>(null) }
    val pendingExpense = (monthExpectedExpense - monthSpentExpense).coerceAtLeast(0)

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("고정관리", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            item {
                FixedSummaryCard(
                    expectedExpense = monthExpectedExpense,
                    spentExpense = monthSpentExpense,
                    pendingExpense = pendingExpense,
                )
            }
            item {
                Text(
                    text = "고정금액 목록 ${rows.size}건",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (rows.isEmpty()) {
                item {
                    Text(
                        text = "등록된 고정금액이 없습니다. + 버튼으로 추가해 주세요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(items = rows, key = { it.schedule.id }) { row ->
                    FixedScheduleMoneyBox(
                        row = row,
                        onToggleActive = { active ->
                            onUpdate(
                                row.schedule.copy(
                                    isActive = active,
                                    updatedAt = System.currentTimeMillis(),
                                )
                            )
                        },
                        onEdit = {
                            editingSchedule = row.schedule
                            showDialog = true
                        },
                        onDelete = { onDelete(row.schedule) },
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            onClick = {
                editingSchedule = null
                showDialog = true
            },
        ) {
            Icon(Icons.Default.Add, contentDescription = "고정금액 추가")
        }
    }

    if (showDialog) {
        FixedScheduleEditDialog(
            categories = categories,
            initial = editingSchedule,
            onDismiss = { showDialog = false },
            onConfirm = { kind, categoryId, amount, memo, dayOfMonth, triggerHour, startYearMonth ->
                val editing = editingSchedule
                if (editing == null) {
                    onAdd(kind, categoryId, amount, memo, dayOfMonth, triggerHour, startYearMonth)
                } else {
                    onUpdate(
                        editing.copy(
                            kind = kind.key,
                            categoryId = categoryId,
                            amount = amount,
                            memo = memo,
                            dayOfMonth = dayOfMonth,
                            triggerHour = triggerHour,
                            startYearMonth = startYearMonth,
                            updatedAt = System.currentTimeMillis(),
                        )
                    )
                }
                showDialog = false
                editingSchedule = null
            },
        )
    }
}

@Composable
private fun FixedSummaryCard(
    expectedExpense: Int,
    spentExpense: Int,
    pendingExpense: Int,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF11C78B)),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text("이번달 고정관리", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            FixedSummaryMiniCard(
                modifier = Modifier.fillMaxWidth(),
                title = "이번달 총 고정 지출 예상 금액",
                amount = expectedExpense,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FixedSummaryMiniCard(
                    modifier = Modifier.weight(1f),
                    title = "이번달 지출금액",
                    amount = spentExpense,
                )
                FixedSummaryMiniCard(
                    modifier = Modifier.weight(1f),
                    title = "이번달 지출 예상인 금액",
                    amount = pendingExpense,
                )
            }
        }
    }
}

@Composable
private fun FixedSummaryMiniCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: Int,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.16f)),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${formatMoney(amount)}원",
                color = Color(0xFFFFE0E0),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun FixedScheduleMoneyBox(
    row: FixedScheduleRow,
    onToggleActive: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val schedule = row.schedule
    val type = TransactionType.fromKey(schedule.kind)
    val isIncome = type == TransactionType.FIXED_INCOME
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isIncome) Color(0xFFE9FFF3) else Color(0xFFFFF1F1),
        ),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CategoryIconDisplay(
                        iconKey = row.categoryIconKey,
                        modifier = Modifier.width(34.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = schedule.memo?.takeIf { it.isNotBlank() } ?: row.categoryName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${if (isIncome) "고정 수입" else "고정 지출"} · 매월 ${schedule.dayOfMonth}일 ${schedule.triggerHour}시",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    text = "${if (isIncome) "+" else "-"}${formatMoney(schedule.amount)}원",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) Color(0xFF00B874) else Color(0xFFFF6363),
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("활성", style = MaterialTheme.typography.bodySmall)
                    Switch(
                        checked = schedule.isActive,
                        onCheckedChange = onToggleActive,
                    )
                }
                Row {
                    TextButton(onClick = onEdit) { Text("수정") }
                    TextButton(onClick = onDelete) { Text("삭제") }
                }
            }
        }
    }
}

@Composable
private fun FixedScheduleEditDialog(
    categories: List<CategoryEntity>,
    initial: FixedScheduleEntity?,
    onDismiss: () -> Unit,
    onConfirm: (
        kind: TransactionType,
        categoryId: Long,
        amount: Int,
        memo: String?,
        dayOfMonth: Int,
        triggerHour: Int,
        startYearMonth: String,
    ) -> Unit,
) {
    var kind by rememberSaveable {
        mutableStateOf(
            TransactionType.fromKey(initial?.kind ?: TransactionType.FIXED_EXPENSE.key).let {
                if (it == TransactionType.FIXED_INCOME) TransactionType.FIXED_INCOME else TransactionType.FIXED_EXPENSE
            }
        )
    }
    var categoryId by rememberSaveable { mutableStateOf(initial?.categoryId ?: categories.firstOrNull()?.id ?: 0L) }
    var amountInput by rememberSaveable { mutableStateOf(initial?.amount?.toString() ?: "") }
    var memo by rememberSaveable { mutableStateOf(initial?.memo ?: "") }
    var dayInput by rememberSaveable { mutableStateOf((initial?.dayOfMonth ?: 10).toString()) }
    val nowYm = remember { YearMonth.now(ZoneId.of("Asia/Seoul")).format(DateTimeFormatter.ofPattern("yyyy-MM")) }
    var startYm by rememberSaveable { mutableStateOf(initial?.startYearMonth ?: nowYm) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "고정금액 추가" else "고정금액 수정") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FixedTypeChip(
                        label = "고정 지출",
                        selected = kind == TransactionType.FIXED_EXPENSE,
                        onClick = { kind = TransactionType.FIXED_EXPENSE },
                    )
                    FixedTypeChip(
                        label = "고정 수입",
                        selected = kind == TransactionType.FIXED_INCOME,
                        onClick = { kind = TransactionType.FIXED_INCOME },
                    )
                }
                CategoryDropDown(
                    categories = categories,
                    selectedCategoryId = categoryId,
                    onSelect = { categoryId = it },
                )
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("금액") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("메모") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = dayInput,
                    onValueChange = { dayInput = it.filter(Char::isDigit) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("매월 실행 일자(1~31)") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = startYm,
                    onValueChange = { startYm = it.take(7) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("시작 월 (yyyy-MM)") },
                    singleLine = true,
                )
                Text(
                    text = "실행 시간은 한국시간 14:00 고정입니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = amountInput.toIntOrNull() ?: 0
                    val day = dayInput.toIntOrNull()?.coerceIn(1, 31) ?: 1
                    if (categoryId == 0L || amount <= 0) return@TextButton
                    onConfirm(
                        kind,
                        categoryId,
                        amount,
                        memo.trim().ifBlank { null },
                        day,
                        14,
                        startYm,
                    )
                }
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        },
    )
}

@Composable
private fun CategoryDropDown(
    categories: List<CategoryEntity>,
    selectedCategoryId: Long,
    onSelect: (Long) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedName = categories.firstOrNull { it.id == selectedCategoryId }?.name ?: "카테고리 선택"
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true },
    ) {
        Text(
            text = selectedName,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        categories.forEach { category ->
            DropdownMenuItem(
                text = { Text(category.name) },
                onClick = {
                    onSelect(category.id)
                    expanded = false
                },
            )
        }
    }
}

@Composable
private fun FixedTypeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = if (selected) Color(0xFF11C78B).copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color(0xFF0AA870) else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatMoney(amount: Int): String =
    com.jaehwan.moneybook.transaction.domain.formatMoney(amount)
