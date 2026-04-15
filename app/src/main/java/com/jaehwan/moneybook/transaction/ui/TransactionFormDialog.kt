package com.jaehwan.moneybook.transaction.ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.jaehwan.moneybook.category.data.local.CategoryEntity
import com.jaehwan.moneybook.transaction.data.local.TransactionEntity
import com.jaehwan.moneybook.transaction.domain.model.TransactionType
import com.jaehwan.moneybook.ui.focusScrollToVerticalBiasInViewport
import java.text.NumberFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormDialog(
    initial: TransactionEntity?,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onConfirm: (TransactionEntity) -> Unit,
    /** SPLIT 선택 시 금액·카테고리·메모만 넘기고 뿜빠이 상세 화면으로 보냄 */
    onRequestSplitDetails: ((amount: Int, categoryId: Long, memo: String?) -> Unit)? = null,
) {
    val tag = "TxFormDropdown"
    val isEdit = initial != null
    val title = if (isEdit) "거래 수정" else "거래 추가"

    var selectedType by remember(initial?.id) {
        mutableStateOf(initial?.let { TransactionType.fromKey(it.type) } ?: TransactionType.EXPENSE)
    }
    var amountText by remember(initial?.id) {
        mutableStateOf(initial?.amount?.let { formatAmountInput(it.toString()) }.orEmpty())
    }
    var categoryId by remember(initial?.id) {
        mutableStateOf(
            initial?.categoryId ?: categories.firstOrNull()?.id ?: 0L
        )
    }
    var memoText by remember(initial?.id) {
        mutableStateOf(initial?.memo.orEmpty())
    }
    var isConfirmed by remember(initial?.id) {
        mutableStateOf(initial?.isConfirmed ?: true)
    }
    var hasAlarm by remember(initial?.id) {
        mutableStateOf(initial?.hasAlarm ?: false)
    }
    var expectedDateMillis by remember(initial?.id) {
        mutableLongStateOf(initial?.expectedDate ?: startOfDayMillis())
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    val formScrollState = rememberScrollState()
    val formScrollScope = rememberCoroutineScope()
    var formViewportCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    LaunchedEffect(categories, initial?.id) {
        if (categories.isEmpty()) {
            categoryId = 0L
            return@LaunchedEffect
        }
        val exists = categories.any { it.id == categoryId }
        if (!exists) {
            categoryId = initial?.categoryId ?: categories.first().id
        }
    }

    LaunchedEffect(categoryExpanded) {
        Log.d(tag, "categoryExpanded changed=$categoryExpanded")
    }
    LaunchedEffect(categoryId) {
        Log.d(tag, "categoryId changed=$categoryId")
    }
    LaunchedEffect(categories.size) {
        Log.d(tag, "categories size=${categories.size}")
    }

    val dateLabel = remember(expectedDateMillis) {
        val z = ZoneId.systemDefault()
        val d = Instant.ofEpochMilli(expectedDateMillis).atZone(z).toLocalDate()
        DateTimeFormatter.ISO_LOCAL_DATE.format(d)
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = expectedDateMillis
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { expectedDateMillis = it }
                        showDatePicker = false
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("취소")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            if (categories.isEmpty()) {
                Text("카테고리를 먼저 추가한 뒤 거래를 등록할 수 있습니다.")
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .verticalScroll(formScrollState)
                        .onGloballyPositioned { formViewportCoords = it }
                ) {
                    Text("종류", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(8.dp))
                    TransactionType.entries.chunked(3).forEach { chunk ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            chunk.forEach { type ->
                                FilterChip(
                                    selected = selectedType == type,
                                    onClick = {
                                        selectedType = type
                                        if (!type.isFixed) {
                                            isConfirmed = true
                                            hasAlarm = false
                                        } else if (initial == null) {
                                            isConfirmed = false
                                        }
                                    },
                                    label = { Text(type.label, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = formatAmountInput(it) },
                        label = { Text("금액") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusScrollToVerticalBiasInViewport(
                                scrollState = formScrollState,
                                viewportCoordinates = { formViewportCoords },
                                coroutineScope = formScrollScope,
                            )
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Box(modifier = Modifier.fillMaxWidth()) {
                        val label = categories.find { it.id == categoryId }?.name ?: "선택"
                        Log.d(tag, "render category field label=$label expanded=$categoryExpanded")
                        OutlinedTextField(
                            value = label,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("카테고리") },
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        Log.d(tag, "category trailing icon clicked before=$categoryExpanded")
                                        categoryExpanded = !categoryExpanded
                                        Log.d(tag, "category trailing icon clicked after=$categoryExpanded")
                                    }
                                ) {
                                    Text(
                                        if (categoryExpanded) "▲" else "▼",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusScrollToVerticalBiasInViewport(
                                    scrollState = formScrollState,
                                    viewportCoordinates = { formViewportCoords },
                                    coroutineScope = formScrollScope,
                                )
                        )
                    }
                    Log.d(tag, "render dropdown visible=$categoryExpanded")
                    AnimatedVisibility(visible = categoryExpanded) {
                        Log.d(tag, "dropdown content composing categories=${categories.size}")
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        ) {
                            Column {
                                categories.forEachIndexed { idx, cat ->
                                    TextButton(
                                        onClick = {
                                            Log.d(tag, "category item clicked id=${cat.id} name=${cat.name}")
                                            categoryId = cat.id
                                            categoryExpanded = false
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = cat.name,
                                            modifier = Modifier.fillMaxWidth(),
                                            color = if (cat.id == categoryId) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    if (idx != categories.lastIndex) {
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = memoText,
                        onValueChange = { memoText = it },
                        label = { Text("메모 (선택)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusScrollToVerticalBiasInViewport(
                                scrollState = formScrollState,
                                viewportCoordinates = { formViewportCoords },
                                coroutineScope = formScrollScope,
                            )
                    )

                    if (selectedType.isFixed) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("예정일: $dateLabel", style = MaterialTheme.typography.bodyMedium)
                        TextButton(onClick = { showDatePicker = true }) {
                            Text("예정일 변경")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("실제 수입·지출 반영됨", modifier = Modifier.weight(1f))
                            Switch(checked = isConfirmed, onCheckedChange = { isConfirmed = it })
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("알람 사용", modifier = Modifier.weight(1f))
                            Switch(checked = hasAlarm, onCheckedChange = { hasAlarm = it })
                        }
                    }
                }
            }
        },
        confirmButton = {
            val openSplitFlow =
                selectedType == TransactionType.SPLIT && onRequestSplitDetails != null
            TextButton(
                onClick = {
                    val amount = parseAmountInput(amountText) ?: return@TextButton
                    if (amount <= 0 || categories.isEmpty()) return@TextButton
                    if (categoryId == 0L) return@TextButton
                    if (openSplitFlow) {
                        onRequestSplitDetails.invoke(
                            amount,
                            categoryId,
                            memoText.trim().ifEmpty { null },
                        )
                        onDismiss()
                        return@TextButton
                    }
                    val now = System.currentTimeMillis()
                    val confirmed = if (selectedType.isFixed) isConfirmed else true
                    val alarm = if (selectedType.isFixed) hasAlarm else false
                    val expected = if (selectedType.isFixed) {
                        expectedDateMillis
                    } else {
                        startOfDayMillis()
                    }
                    val entity = TransactionEntity(
                        id = initial?.id ?: 0,
                        categoryId = categoryId,
                        amount = amount,
                        type = selectedType.key,
                        isConfirmed = confirmed,
                        expectedDate = expected,
                        hasAlarm = alarm,
                        memo = memoText.trim().ifEmpty { null },
                        createdAt = initial?.createdAt ?: now,
                        updatedAt = now,
                    )
                    onConfirm(entity)
                },
                enabled = categories.isNotEmpty() &&
                    parseAmountInput(amountText)?.let { it > 0 } == true &&
                    categoryId != 0L
            ) {
                Text(
                    when {
                        openSplitFlow -> "뿜빠이 상세 입력"
                        isEdit -> "저장"
                        else -> "추가"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}

private fun parseAmountInput(value: String): Int? =
    value.replace(",", "").toIntOrNull()

private fun formatAmountInput(value: String): String {
    val digits = value.filter { it.isDigit() }
    if (digits.isEmpty()) return ""
    val normalized = digits.trimStart('0').ifEmpty { "0" }
    val asLong = normalized.toLongOrNull() ?: return normalized
    return NumberFormat.getNumberInstance(Locale.KOREA).format(asLong)
}
