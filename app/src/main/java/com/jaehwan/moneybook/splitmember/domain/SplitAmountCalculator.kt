package com.jaehwan.moneybook.splitmember.domain

/**
 * 총액을 [count]명에게 **원 단위 균등 분배**(나머지는 앞쪽 인원에게 1원씩)한 뒤,
 * 각 멤버의 extra/deduction을 더하고, 합이 [total]이 되도록 [primaryIndex]에 차이를 보정합니다.
 */
fun computeSuggestedShares(
    total: Int,
    count: Int,
    extras: List<Int>,
    deductions: List<Int>,
    primaryIndex: Int = 0,
): List<Int> {
    if (count <= 0 || extras.size != count || deductions.size != count) {
        return List(count) { 0 }
    }
    val base = total / count
    val remainder = total % count
    val raw = extras.indices.map { i ->
        base + (if (i < remainder) 1 else 0) + extras[i] - deductions[i]
    }.toMutableList()
    val sum = raw.sum()
    var diff = total - sum
    val safePrimary = primaryIndex.coerceIn(0, count - 1)
    raw[safePrimary] = raw[safePrimary] + diff
    return raw
}
