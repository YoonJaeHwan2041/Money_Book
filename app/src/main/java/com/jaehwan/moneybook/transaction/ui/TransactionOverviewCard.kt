package com.jaehwan.moneybook.transaction.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
fun TransactionOverviewCard(
    balance: Int,
    income: Int,
    expense: Int,
    installment: Int,
    installmentSummary: InstallmentSummary,
    rawBalanceTitle: String = "지금 잔고",
) {
    var showInstallment by rememberSaveable { mutableStateOf(true) }
    var showMode by rememberSaveable { mutableStateOf(OverviewMode.WithInstallment) }
    val adjustedBalance = balance - installmentSummary.remainingTotal
    val mainBalance = if (showMode == OverviewMode.WithInstallment) adjustedBalance else balance

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF11C78B)),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = if (showMode == OverviewMode.WithInstallment) "할부금 포함 금액" else rawBalanceTitle,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Text(
                text = "${if (mainBalance < 0) "-" else ""}${formatMoney(kotlin.math.abs(mainBalance))}원",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold,
            )
            if (showInstallment) {
                Spacer(modifier = Modifier.padding(2.dp))
                Text(
                    text = "-${formatMoney(installmentSummary.remainingTotal)}원 · 할부 ${installmentSummary.activeCount}건",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color(0xFFFFD6D6),
                )
            }
            Spacer(modifier = Modifier.padding(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OverviewMode.entries.forEach { mode ->
                        OverviewModeChip(
                            label = mode.label,
                            selected = showMode == mode,
                            onClick = { showMode = mode },
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("할부 표시", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    Switch(
                        checked = showInstallment,
                        onCheckedChange = { showInstallment = it },
                    )
                }
            }
            Spacer(modifier = Modifier.padding(6.dp))
            OverviewMiniCard(
                modifier = Modifier.fillMaxWidth(),
                title = "총 수입",
                amount = income,
                positive = true,
            )
            Spacer(modifier = Modifier.padding(3.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OverviewMiniCard(
                    modifier = Modifier.weight(1f),
                    title = "총 지출",
                    amount = expense,
                    positive = false,
                )
                OverviewMiniCard(
                    modifier = Modifier.weight(1f),
                    title = "총 할부",
                    amount = installment,
                    positive = false,
                )
            }
        }
    }
}

private enum class OverviewMode(val label: String) {
    WithInstallment("할부금 포함 금액"),
    RawBalance("지금 잔고"),
}

@Composable
private fun OverviewModeChip(
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
private fun OverviewMiniCard(
    modifier: Modifier = Modifier,
    title: String,
    amount: Int,
    positive: Boolean,
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
            Spacer(modifier = Modifier.padding(2.dp))
            Text(
                text = "${if (positive) "+" else "-"}${formatMoney(amount)}원",
                color = if (positive) Color(0xFFD6FFED) else Color(0xFFFFE0E0),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private fun formatMoney(amount: Int): String =
    com.jaehwan.moneybook.transaction.domain.formatMoney(amount)
