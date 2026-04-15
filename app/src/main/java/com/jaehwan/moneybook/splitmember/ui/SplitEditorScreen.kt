package com.jaehwan.moneybook.splitmember.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.jaehwan.moneybook.splitmember.data.local.SplitMemberEntity
import com.jaehwan.moneybook.splitmember.domain.computeSuggestedShares
import com.jaehwan.moneybook.transaction.data.local.TransactionEntity
import com.jaehwan.moneybook.transaction.domain.model.TransactionType
import com.jaehwan.moneybook.transaction.ui.LedgerViewModel
import com.jaehwan.moneybook.transaction.ui.startOfDayMillis
import com.jaehwan.moneybook.ui.focusScrollToVerticalBiasInViewport

private data class MemberRowState(
    val name: String,
    val extraText: String = "0",
    val deductionText: String = "0",
    val agreedText: String = "",
    val useSuggested: Boolean = true,
    val memo: String = "",
    val isPrimary: Boolean,
    val isPaid: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitEditorScreen(
    initialTransaction: TransactionEntity?,
    prefilledTotal: Int,
    prefilledCategoryId: Long,
    prefilledMemo: String?,
    ledgerViewModel: LedgerViewModel,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity, List<SplitMemberEntity>) -> Unit,
) {
    val total = prefilledTotal
    var memberCount by remember(initialTransaction?.id) { mutableIntStateOf(2) }
    var rowStates by remember(initialTransaction?.id) {
        mutableStateOf<List<MemberRowState>>(emptyList())
    }

    LaunchedEffect(initialTransaction?.id) {
        if (initialTransaction != null) {
            val loaded = ledgerViewModel.getSplitMembers(initialTransaction.id)
            rowStates = loaded.map { m ->
                MemberRowState(
                    name = m.memberName,
                    extraText = m.extraAmount.toString(),
                    deductionText = m.deductionAmount.toString(),
                    agreedText = m.agreedAmount?.toString().orEmpty(),
                    useSuggested = m.agreedAmount == null,
                    memo = m.paymentMemo.orEmpty(),
                    isPrimary = m.isPrimaryPayer,
                    isPaid = m.isPaid,
                )
            }
            if (rowStates.isNotEmpty()) {
                memberCount = rowStates.size
            }
        }
    }

    LaunchedEffect(memberCount, initialTransaction?.id) {
        if (initialTransaction != null) return@LaunchedEffect
        rowStates = List(memberCount) { i ->
            MemberRowState(
                name = if (i == 0) "본인" else "멤버${i + 1}",
                isPrimary = i == 0,
                isPaid = false,
            )
        }
    }

    var memoTargetIndex by remember { mutableStateOf<Int?>(null) }
    var memoDraft by remember { mutableStateOf("") }

    val extras = rowStates.map { it.extraText.toIntOrNull() ?: 0 }
    val deductions = rowStates.map { it.deductionText.toIntOrNull() ?: 0 }
    val suggested = remember(total, memberCount, rowStates) {
        if (memberCount <= 0 || rowStates.size != memberCount) emptyList()
        else computeSuggestedShares(total, memberCount, extras, deductions, primaryIndex = 0)
    }
    val formScrollState = rememberScrollState()
    val formScrollScope = rememberCoroutineScope()
    var formViewportCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text(if (initialTransaction == null) "뿜빠이 추가" else "뿜빠이 수정") },
                    navigationIcon = {
                        TextButton(onClick = onDismiss) { Text("닫기") }
                    },
                    actions = {
                        TextButton(
                            onClick = {
                                if (rowStates.size != memberCount || total <= 0) return@TextButton
                                val now = System.currentTimeMillis()
                                val tx = TransactionEntity(
                                    id = initialTransaction?.id ?: 0,
                                    categoryId = prefilledCategoryId,
                                    amount = total,
                                    type = TransactionType.SPLIT.key,
                                    isConfirmed = true,
                                    expectedDate = startOfDayMillis(),
                                    hasAlarm = false,
                                    memo = prefilledMemo?.trim()?.ifEmpty { null },
                                    createdAt = initialTransaction?.createdAt ?: now,
                                    updatedAt = now,
                                )
                                val members = rowStates.mapIndexed { index, r ->
                                    val sug = suggested.getOrElse(index) { 0 }
                                    val agreed = if (r.useSuggested) sug else (r.agreedText.toIntOrNull() ?: sug)
                                    SplitMemberEntity(
                                        id = 0,
                                        transactionId = 0,
                                        memberName = r.name.trim().ifEmpty {
                                            if (index == 0) "본인" else "멤버${index + 1}"
                                        },
                                        isPrimaryPayer = index == 0,
                                        extraAmount = r.extraText.toIntOrNull() ?: 0,
                                        deductionAmount = r.deductionText.toIntOrNull() ?: 0,
                                        agreedAmount = agreed,
                                        isPaid = r.isPaid,
                                        paymentMemo = r.memo.trim().ifEmpty { null },
                                        createdAt = now,
                                        updatedAt = now,
                                    )
                                }
                                onSave(tx, members)
                            }
                        ) {
                            Text("저장")
                        }
                    }
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .imePadding()
                        .verticalScroll(formScrollState)
                        .onGloballyPositioned { formViewportCoords = it }
                        .padding(16.dp)
                ) {
                    Text("총액 ${total}원", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "본인(맨 위)이 이미 총액을 결제했다는 전제로, 아래 인원에게 받을 금액을 나눕니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    if (initialTransaction == null) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("인원 수", modifier = Modifier.weight(1f))
                            TextButton(onClick = { if (memberCount > 2) memberCount-- }) { Text("-") }
                            Text(memberCount.toString(), style = MaterialTheme.typography.titleMedium)
                            TextButton(onClick = { if (memberCount < 20) memberCount++ }) { Text("+") }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    rowStates.forEachIndexed { index, row ->
                        val sug = suggested.getOrElse(index) { 0 }
                        Text(
                            if (row.isPrimary) "본인 (총액 결제)" else "멤버 ${index + 1}",
                            style = MaterialTheme.typography.labelLarge
                        )
                        if (!row.isPrimary) {
                            OutlinedTextField(
                                value = row.name,
                                onValueChange = { v ->
                                    rowStates = rowStates.mapIndexed { j, r ->
                                        if (j == index) r.copy(name = v) else r
                                    }
                                },
                                label = { Text("이름") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .focusScrollToVerticalBiasInViewport(
                                        scrollState = formScrollState,
                                        viewportCoordinates = { formViewportCoords },
                                        coroutineScope = formScrollScope,
                                    )
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = row.extraText,
                                onValueChange = { v ->
                                    rowStates = rowStates.mapIndexed { j, r ->
                                        if (j == index) r.copy(extraText = v.filter { it.isDigit() }) else r
                                    }
                                },
                                label = { Text("추가") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .focusScrollToVerticalBiasInViewport(
                                        scrollState = formScrollState,
                                        viewportCoordinates = { formViewportCoords },
                                        coroutineScope = formScrollScope,
                                    )
                            )
                            OutlinedTextField(
                                value = row.deductionText,
                                onValueChange = { v ->
                                    rowStates = rowStates.mapIndexed { j, r ->
                                        if (j == index) r.copy(deductionText = v.filter { it.isDigit() }) else r
                                    }
                                },
                                label = { Text("차감") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .focusScrollToVerticalBiasInViewport(
                                        scrollState = formScrollState,
                                        viewportCoordinates = { formViewportCoords },
                                        coroutineScope = formScrollScope,
                                    )
                            )
                        }
                        Text("제안: ${sug}원", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = row.useSuggested,
                                onCheckedChange = { checked ->
                                    rowStates = rowStates.mapIndexed { j, r ->
                                        if (j == index) {
                                            r.copy(
                                                useSuggested = checked,
                                                agreedText = if (checked) sug.toString() else r.agreedText
                                            )
                                        } else r
                                    }
                                }
                            )
                            Text("제안 금액 사용", style = MaterialTheme.typography.bodyMedium)
                        }
                        OutlinedTextField(
                            value = row.agreedText,
                            onValueChange = { v ->
                                rowStates = rowStates.mapIndexed { j, r ->
                                    if (j == index) r.copy(agreedText = v.filter { it.isDigit() }) else r
                                }
                            },
                            label = { Text("확정 금액") },
                            placeholder = { Text("${sug}원") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusScrollToVerticalBiasInViewport(
                                    scrollState = formScrollState,
                                    viewportCoordinates = { formViewportCoords },
                                    coroutineScope = formScrollScope,
                                )
                        )
                        TextButton(onClick = {
                            memoTargetIndex = index
                            memoDraft = row.memo
                        }) {
                            Text(if (row.memo.isBlank()) "메모 추가" else "메모 수정")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    memoTargetIndex?.let { idx ->
        AlertDialog(
            onDismissRequest = { memoTargetIndex = null },
            title = { Text("멤버 메모") },
            text = {
                val memoScrollState = rememberScrollState()
                var memoViewportCoords by remember { mutableStateOf<LayoutCoordinates?>(null) }
                val memoScrollScope = rememberCoroutineScope()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .verticalScroll(memoScrollState)
                        .onGloballyPositioned { memoViewportCoords = it }
                ) {
                    OutlinedTextField(
                        value = memoDraft,
                        onValueChange = { memoDraft = it },
                        label = { Text("메모") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusScrollToVerticalBiasInViewport(
                                scrollState = memoScrollState,
                                viewportCoordinates = { memoViewportCoords },
                                coroutineScope = memoScrollScope,
                            )
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    rowStates = rowStates.mapIndexed { j, r ->
                        if (j == idx) r.copy(memo = memoDraft) else r
                    }
                    memoTargetIndex = null
                }) { Text("확인") }
            },
            dismissButton = {
                TextButton(onClick = { memoTargetIndex = null }) { Text("취소") }
            }
        )
    }
}
