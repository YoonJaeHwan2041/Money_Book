package com.jaehwan.moneybook.transaction.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import com.jaehwan.moneybook.category.data.local.CategoryEntity
import com.jaehwan.moneybook.splitmember.data.local.SplitMemberEntity
import com.jaehwan.moneybook.transaction.data.local.TransactionEntity
import com.jaehwan.moneybook.transaction.domain.model.TransactionType
import com.jaehwan.moneybook.ui.focusScrollToVerticalBiasInViewport
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.max

private data class SplitMemberDraft(
    var name: String,
    var amountText: String,
    var memoText: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionEntryScreen(
    categories: List<CategoryEntity>,
    initial: TransactionEntity?,
    onDismiss: () -> Unit,
    onSaveNormal: (TransactionEntity) -> Unit,
    onSaveSplit: (TransactionEntity, List<SplitMemberEntity>) -> Unit,
) {
    val isEdit = initial != null
    var selectedType by remember(initial?.id) {
        mutableStateOf(initial?.let { TransactionType.fromKey(it.type) } ?: TransactionType.EXPENSE)
    }
    var amountText by remember(initial?.id) { mutableStateOf(initial?.amount?.let { formatAmountInput(it.toString()) }.orEmpty()) }
    var categoryId by remember(initial?.id) { mutableStateOf(initial?.categoryId ?: categories.firstOrNull()?.id ?: 0L) }
    var memoText by remember(initial?.id) { mutableStateOf(initial?.memo.orEmpty()) }
    var isConfirmed by remember(initial?.id) { mutableStateOf(initial?.isConfirmed ?: true) }
    var hasAlarm by remember(initial?.id) { mutableStateOf(initial?.hasAlarm ?: false) }
    var expectedDateMillis by remember(initial?.id) { mutableLongStateOf(initial?.expectedDate ?: startOfDayMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var participantCountText by remember(initial?.id) { mutableStateOf("2") }
    var selfAmountText by remember { mutableStateOf("") }
    var bulkAmountText by remember { mutableStateOf("") }
    var formError by remember { mutableStateOf<String?>(null) }
    val memberDrafts = remember { mutableStateListOf<SplitMemberDraft>() }
    val formScrollState = rememberScrollState()
    val formScrollScope = rememberCoroutineScope()
    var formViewportCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
    val isSplit = selectedType == TransactionType.SPLIT

    LaunchedEffect(categories, initial?.id) {
        if (categories.isNotEmpty() && categories.none { it.id == categoryId }) {
            categoryId = categories.first().id
        }
    }

    val parsedAmount = parseAmountInput(amountText) ?: 0
    val participantCount = participantCountText.toIntOrNull()?.coerceIn(1, 30) ?: 0
    val count = max(participantCount, 1)
    val autoPerHead = ((parsedAmount / count) / 10) * 10

    LaunchedEffect(isSplit, participantCount) {
        if (!isSplit) {
            memberDrafts.clear()
            return@LaunchedEffect
        }
        val targetOthers = max(participantCount - 1, 0)
        while (memberDrafts.size < targetOthers) {
            val idx = memberDrafts.size + 1
            memberDrafts.add(
                SplitMemberDraft(
                    name = "멤버$idx",
                    amountText = "",
                    memoText = "",
                )
            )
        }
        while (memberDrafts.size > targetOthers) {
            memberDrafts.removeAt(memberDrafts.lastIndex)
        }
    }

    val selfAmount = parseAmountInput(selfAmountText) ?: 0
    val othersTotal = memberDrafts.sumOf { parseAmountInput(it.amountText) ?: 0 }
    val distributedTotal = selfAmount + othersTotal
    val droppedAmount = (parsedAmount - distributedTotal).coerceAtLeast(0)
    val dateLabel = remember(expectedDateMillis) {
        val d = Instant.ofEpochMilli(expectedDateMillis).atZone(ZoneId.systemDefault()).toLocalDate()
        DateTimeFormatter.ISO_LOCAL_DATE.format(d)
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = expectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { expectedDateMillis = it }
                    showDatePicker = false
                }) { Text("확인") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("취소") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "거래 수정" else "거래 추가") },
                navigationIcon = { TextButton(onClick = onDismiss) { Text("취소") } },
                actions = {
                    TextButton(
                        onClick = {
                            formError = null
                            val amountError = validateTransactionAmount(amountText)
                            if (amountError != null) {
                                formError = amountError
                                return@TextButton
                            }
                            val amount = parseAmountInput(amountText) ?: return@TextButton
                            if (amount <= 0 || categoryId == 0L) return@TextButton
                            val now = System.currentTimeMillis()
                            val txType = selectedType
                            val entity = TransactionEntity(
                                id = initial?.id ?: 0L,
                                categoryId = categoryId,
                                amount = amount,
                                type = txType.key,
                                isConfirmed = if (txType.isFixed) isConfirmed else true,
                                expectedDate = if (txType.isFixed) expectedDateMillis else startOfDayMillis(),
                                hasAlarm = if (txType.isFixed) hasAlarm else false,
                                memo = memoText.trim().ifEmpty { null },
                                createdAt = initial?.createdAt ?: now,
                                updatedAt = now,
                            )
                            if (txType != TransactionType.SPLIT) {
                                onSaveNormal(entity)
                                return@TextButton
                            }
                            if (memberDrafts.isEmpty()) return@TextButton
                            val splitError = validateSplitInput(
                                SplitDraftValidationInput(
                                    participantCountText = participantCountText,
                                    selfAmountText = selfAmountText,
                                    memberDrafts = memberDrafts.map { draft ->
                                        SplitMemberDraftInput(
                                            name = draft.name,
                                            amountText = draft.amountText,
                                        )
                                    },
                                )
                            )
                            if (splitError != null) {
                                formError = splitError
                                return@TextButton
                            }
                            val members = buildList {
                                add(
                                    SplitMemberEntity(
                                        transactionId = entity.id,
                                        memberName = "나",
                                        isPrimaryPayer = true,
                                        agreedAmount = selfAmount,
                                        isPaid = true,
                                    )
                                )
                                memberDrafts.forEach { draft ->
                                    val memberAmount = parseAmountInput(draft.amountText) ?: 0
                                    if (draft.name.isNotBlank() && memberAmount > 0) {
                                        add(
                                            SplitMemberEntity(
                                                transactionId = entity.id,
                                                memberName = draft.name.trim(),
                                                isPrimaryPayer = false,
                                                agreedAmount = memberAmount,
                                                paymentMemo = draft.memoText.trim().ifEmpty { null },
                                            )
                                        )
                                    }
                                }
                            }
                            if (members.size <= 1) return@TextButton
                            onSaveSplit(entity, members)
                        },
                        enabled = categories.isNotEmpty(),
                    ) { Text(if (isEdit) "저장" else "추가") }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(formScrollState)
                .onGloballyPositioned { formViewportCoords = it },
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("종류", style = MaterialTheme.typography.labelLarge)
            TransactionType.entries.chunked(3).forEach { rowTypes ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rowTypes.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.label) },
                        )
                    }
                }
            }

            if (!formError.isNullOrBlank()) {
                Text(
                    text = formError ?: "",
                    color = Color(0xFFD66A6A),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = formatAmountInput(it)
                    formError = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusScrollToVerticalBiasInViewport(formScrollState, { formViewportCoords }, formScrollScope),
                label = { Text("금액") },
                singleLine = true,
            )
            CategoryPickerField(
                categories = categories,
                categoryId = categoryId,
                onSelect = { categoryId = it },
            )
            OutlinedTextField(
                value = memoText,
                onValueChange = {
                    memoText = it
                    formError = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusScrollToVerticalBiasInViewport(formScrollState, { formViewportCoords }, formScrollScope),
                label = { Text("메모 (선택)") },
            )

            if (selectedType.isFixed) {
                Text("예정일: $dateLabel")
                TextButton(onClick = { showDatePicker = true }) { Text("예정일 변경") }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("실제 수입·지출 반영")
                    Switch(checked = isConfirmed, onCheckedChange = { isConfirmed = it })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("알람 사용")
                    Switch(checked = hasAlarm, onCheckedChange = { hasAlarm = it })
                }
            }

            if (isSplit) {
                HorizontalDivider()
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = participantCountText,
                            onValueChange = {
                                participantCountText = it.filter { ch -> ch.isDigit() }.take(2)
                                formError = null
                            },
                            label = { Text("총 인원 수(본인 포함)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusScrollToVerticalBiasInViewport(formScrollState, { formViewportCoords }, formScrollScope),
                            singleLine = true,
                        )
                        Text("자동 계산(참고): 1인당 ${formatMoney(autoPerHead)}원 (10원 단위 내림)")
                        OutlinedTextField(
                            value = selfAmountText,
                            onValueChange = {
                                selfAmountText = formatAmountInput(it)
                                formError = null
                            },
                            label = { Text("본인 부담금") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusScrollToVerticalBiasInViewport(formScrollState, { formViewportCoords }, formScrollScope),
                            singleLine = true,
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = bulkAmountText,
                                onValueChange = {
                                    bulkAmountText = formatAmountInput(it)
                                    formError = null
                                },
                                label = { Text("전체 입력 금액(본인 제외)") },
                                modifier = Modifier
                                    .weight(1f)
                                    .focusScrollToVerticalBiasInViewport(formScrollState, { formViewportCoords }, formScrollScope),
                                singleLine = true,
                            )
                            TextButton(
                                onClick = {
                                    val bulk = formatAmountInput(bulkAmountText)
                                    memberDrafts.forEach { it.amountText = bulk }
                                },
                                modifier = Modifier.padding(top = 10.dp),
                            ) { Text("적용") }
                        }
                        if (droppedAmount > 0) {
                            Text(
                                text = "버림 값 ${formatMoney(droppedAmount)}원이 남아 있습니다.",
                                color = Color(0xFFD66A6A),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        memberDrafts.forEachIndexed { idx, draft ->
                            HorizontalDivider()
                            OutlinedTextField(
                                value = draft.name,
                                onValueChange = {
                                    draft.name = it
                                    formError = null
                                },
                                label = { Text("멤버 ${idx + 1} 이름") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusScrollToVerticalBiasInViewport(formScrollState, { formViewportCoords }, formScrollScope),
                                singleLine = true,
                            )
                            OutlinedTextField(
                                value = draft.amountText,
                                onValueChange = {
                                    draft.amountText = formatAmountInput(it)
                                    formError = null
                                },
                                label = { Text("멤버 ${idx + 1} 금액") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusScrollToVerticalBiasInViewport(formScrollState, { formViewportCoords }, formScrollScope),
                                singleLine = true,
                            )
                            OutlinedTextField(
                                value = draft.memoText,
                                onValueChange = {
                                    draft.memoText = it
                                    formError = null
                                },
                                label = { Text("멤버 ${idx + 1} 메모 (선택)") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusScrollToVerticalBiasInViewport(formScrollState, { formViewportCoords }, formScrollScope),
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.ime))
        }
    }
}

@Composable
private fun CategoryPickerField(
    categories: List<CategoryEntity>,
    categoryId: Long,
    onSelect: (Long) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = categories.find { it.id == categoryId }?.name ?: "선택"
    Column {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("카테고리") },
            modifier = Modifier
                .fillMaxWidth(),
            trailingIcon = {
                TextButton(onClick = { expanded = !expanded }) {
                    Text(if (expanded) "▲" else "▼")
                }
            }
        )
        if (expanded) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                categories.forEachIndexed { index, category ->
                    TextButton(
                        onClick = {
                            onSelect(category.id)
                            expanded = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(category.name, modifier = Modifier.fillMaxWidth())
                    }
                    if (index != categories.lastIndex) HorizontalDivider()
                }
            }
        }
    }
}

private fun parseAmountInput(value: String): Int? =
    com.jaehwan.moneybook.transaction.domain.parseMoneyInput(value)

private fun formatAmountInput(value: String): String {
    return com.jaehwan.moneybook.transaction.domain.formatMoneyInput(value)
}

private fun formatMoney(amount: Int): String =
    com.jaehwan.moneybook.transaction.domain.formatMoney(amount)

private fun startOfDayMillis(): Long =
    java.time.LocalDate.now()
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
