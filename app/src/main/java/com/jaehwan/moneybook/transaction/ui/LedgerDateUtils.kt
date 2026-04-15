package com.jaehwan.moneybook.transaction.ui

import java.time.Instant
import java.time.ZoneId

fun startOfDayMillis(
    epochMillis: Long = System.currentTimeMillis(),
    zone: ZoneId = ZoneId.systemDefault(),
): Long {
    val day = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
    return day.atStartOfDay(zone).toInstant().toEpochMilli()
}
