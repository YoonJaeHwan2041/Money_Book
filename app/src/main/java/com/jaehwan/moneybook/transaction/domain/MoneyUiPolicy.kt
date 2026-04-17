package com.jaehwan.moneybook.transaction.domain

import com.jaehwan.moneybook.splitmember.data.local.SplitMemberEntity
import java.text.NumberFormat
import java.util.Locale

fun parseMoneyInput(value: String): Int? =
    value.replace(",", "").toIntOrNull()

fun formatMoneyInput(value: String): String {
    val digits = value.filter { it.isDigit() }
    if (digits.isEmpty()) return ""
    val normalized = digits.trimStart('0').ifEmpty { "0" }
    val asLong = normalized.toLongOrNull() ?: return normalized
    return NumberFormat.getNumberInstance(Locale.KOREA).format(asLong)
}

fun formatMoney(amount: Int): String =
    NumberFormat.getNumberInstance(Locale.KOREA).format(amount)

fun isSplitComplete(members: List<SplitMemberEntity>): Boolean {
    val targets = members.filterNot { it.isPrimaryPayer }
    return targets.isNotEmpty() && targets.all { it.isPaid }
}

fun unpaidTotal(members: List<SplitMemberEntity>): Int =
    members
        .filterNot { it.isPrimaryPayer }
        .filterNot { it.isPaid }
        .sumOf { it.agreedAmount ?: 0 }
