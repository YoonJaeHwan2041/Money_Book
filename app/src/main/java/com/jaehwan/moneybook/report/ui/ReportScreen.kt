package com.jaehwan.moneybook.report.ui

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jaehwan.moneybook.category.ui.CategoryIconDisplay
import com.jaehwan.moneybook.report.model.ReportCategoryDeltaItem
import com.jaehwan.moneybook.report.model.ReportCategorySpendItem
import com.jaehwan.moneybook.report.model.ReportForecast
import com.jaehwan.moneybook.report.model.ReportMonthCompare
import com.jaehwan.moneybook.report.model.ReportTopTransaction
import com.jaehwan.moneybook.transaction.ui.MonthSelectorBar
import com.jaehwan.moneybook.transaction.ui.YearMonthPickerDialog
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Composable
fun ReportScreen(
    selectedMonth: YearMonth,
    forecast: ReportForecast,
    categorySpending: List<ReportCategorySpendItem>,
    topTransaction: ReportTopTransaction?,
    monthCompare: ReportMonthCompare,
    categoryCompare: List<ReportCategoryDeltaItem>,
    onChangeMonth: (YearMonth) -> Unit,
) {
    var showMonthPicker by remember { mutableStateOf(false) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text("리포트", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
        item {
            MonthSelectorBar(
                selectedMonth = selectedMonth,
                onChangeMonth = onChangeMonth,
                onOpenPicker = { showMonthPicker = true },
            )
        }
        item { ForecastCard(forecast = forecast) } // 5
        item { CategorySpendCard(items = categorySpending) } // 1
        item { TopTransactionCard(top = topTransaction) } // 2
        item { DisabledCompareCard(title = "저번달과 비교 소비 금액") } // 3 (비활성화)
        item { DisabledCompareCard(title = "저번달과 비교 카테고리 금액") } // 4 (비활성화)
    }
    if (showMonthPicker) {
        YearMonthPickerDialog(
            initial = selectedMonth,
            onDismiss = { showMonthPicker = false },
            onConfirm = {
                onChangeMonth(it)
                showMonthPicker = false
            },
        )
    }
}

@Composable
private fun ForecastCard(forecast: ReportForecast) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF11C78B)),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("다음달 예상금액", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "${if (forecast.forecastAmount < 0) "-" else ""}${formatMoney(abs(forecast.forecastAmount))}원",
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ForecastMini("현재 금액", forecast.currentBalance, true, Modifier.weight(1f))
                ForecastMini("고정수입", forecast.nextMonthFixedIncome, true, Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ForecastMini("고정지출", forecast.nextMonthFixedExpense, false, Modifier.weight(1f))
                ForecastMini("할부예정", forecast.nextMonthInstallmentDue, false, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ForecastMini(title: String, amount: Int, positive: Boolean, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.16f)),
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(title, color = Color.White, style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${if (positive) "+" else "-"}${formatMoney(amount)}원",
                color = if (positive) Color(0xFFD6FFED) else Color(0xFFFFE0E0),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun CategorySpendCard(items: List<ReportCategorySpendItem>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("각월당 소모 카테고리", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (items.isEmpty()) {
                Text("데이터가 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                items.forEach { item ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            CategoryIconDisplay(iconKey = item.iconKey, modifier = Modifier.width(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(item.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        Text(
                            "${formatMoney(item.amount)}원 · ${"%.1f".format(item.ratio)}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopTransactionCard(top: ReportTopTransaction?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("각월당 가장 많은 금액의 지출", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            if (top == null) {
                Text("데이터가 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                Text(top.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${top.categoryName} · ${formatDate(top.expectedDate)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    "${formatMoney(top.amount)}원",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

@Composable
private fun DisabledCompareCard(title: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("현재 비활성화됨", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private fun formatMoney(amount: Int): String =
    com.jaehwan.moneybook.transaction.domain.formatMoney(amount)

private fun formatDate(epochMillis: Long): String {
    val date = Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    return DateTimeFormatter.ISO_LOCAL_DATE.format(date)
}
