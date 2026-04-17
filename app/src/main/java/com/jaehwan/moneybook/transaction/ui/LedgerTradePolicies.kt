package com.jaehwan.moneybook.transaction.ui

import com.jaehwan.moneybook.transaction.domain.model.TransactionType
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId

data class MonthlyTotals(
    val income: Int,
    val expense: Int,
)

fun LedgerRow.isInMonth(month: YearMonth, zoneId: ZoneId = ZoneId.systemDefault()): Boolean {
    val date = Instant.ofEpochMilli(transaction.expectedDate).atZone(zoneId).toLocalDate()
    return date.year == month.year && date.monthValue == month.monthValue
}

fun calculateMonthlyTotals(rows: List<LedgerRow>): MonthlyTotals {
    val income = rows.sumOf { row ->
        when (TransactionType.fromKey(row.transaction.type)) {
            TransactionType.INCOME -> row.transaction.amount
            TransactionType.FIXED_INCOME -> if (row.transaction.isConfirmed) row.transaction.amount else 0
            else -> 0
        }
    }
    val expense = rows.sumOf { row ->
        when (TransactionType.fromKey(row.transaction.type)) {
            TransactionType.EXPENSE, TransactionType.SPLIT -> row.transaction.amount
            TransactionType.FIXED_EXPENSE -> if (row.transaction.isConfirmed) row.transaction.amount else 0
            else -> 0
        }
    }
    return MonthlyTotals(income = income, expense = expense)
}

fun calculateCurrentBalance(rows: List<LedgerRow>): Int =
    rows.sumOf { row ->
        val amount = row.transaction.amount
        when (TransactionType.fromKey(row.transaction.type)) {
            TransactionType.INCOME -> amount
            TransactionType.EXPENSE, TransactionType.SPLIT -> -amount
            TransactionType.FIXED_INCOME -> if (row.transaction.isConfirmed) amount else 0
            TransactionType.FIXED_EXPENSE -> if (row.transaction.isConfirmed) -amount else 0
        }
    }
