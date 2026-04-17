package com.jaehwan.moneybook.transaction.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthSelectorBar(
    selectedMonth: YearMonth,
    onChangeMonth: (YearMonth) -> Unit,
    onOpenPicker: () -> Unit,
    monthText: String = "${selectedMonth.year}년 ${selectedMonth.month.getDisplayName(TextStyle.FULL, Locale.KOREAN)}",
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clickable { onChangeMonth(selectedMonth.minusMonths(1)) },
        ) {
            Text("〈", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = monthText,
            modifier = Modifier.clickable(onClick = onOpenPicker),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clickable { onChangeMonth(selectedMonth.plusMonths(1)) },
        ) {
            Text("〉", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
        }
    }
}

@Composable
fun YearMonthPickerDialog(
    initial: YearMonth,
    onDismiss: () -> Unit,
    onConfirm: (YearMonth) -> Unit,
) {
    var year by remember(initial) { mutableIntStateOf(initial.year) }
    var month by remember(initial) { mutableIntStateOf(initial.monthValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("년/월 선택") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                StepperRow(
                    label = "년도",
                    value = "${year}년",
                    onDecrease = { year -= 1 },
                    onIncrease = { year += 1 },
                )
                StepperRow(
                    label = "월",
                    value = "${month}월",
                    onDecrease = { month = if (month == 1) 12 else month - 1 },
                    onIncrease = { month = if (month == 12) 1 else month + 1 },
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(YearMonth.of(year, month)) }) {
                Text("확인")
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
private fun StepperRow(
    label: String,
    value: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onDecrease) { Text("-") }
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onIncrease) { Text("+") }
        }
    }
}
