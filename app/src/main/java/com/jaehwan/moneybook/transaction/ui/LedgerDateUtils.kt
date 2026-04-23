package com.jaehwan.moneybook.transaction.ui

import java.time.Instant
import java.time.ZoneId

/**
 * 사용자가 고른 날짜(자정 기준 epoch)에 **현재 시각**(같은 ZoneId의 LocalTime)을 합친 epoch ms.
 */
fun mergeSelectedDateWithCurrentTime(
    selectedDateEpochMillis: Long,
    zone: ZoneId = ZoneId.systemDefault(),
): Long {
    val selectedDate = Instant.ofEpochMilli(selectedDateEpochMillis).atZone(zone).toLocalDate()
    val now = java.time.ZonedDateTime.now(zone)
    return selectedDate.atTime(now.toLocalTime()).atZone(zone).toInstant().toEpochMilli()
}

fun startOfDayMillis(
    epochMillis: Long = System.currentTimeMillis(),
    zone: ZoneId = ZoneId.systemDefault(),
): Long {
    val day = Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
    return day.atStartOfDay(zone).toInstant().toEpochMilli()
}
