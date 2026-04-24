package com.jaehwan.moneybook.fixed.domain.repository

import com.jaehwan.moneybook.fixed.data.local.FixedScheduleEntity
import kotlinx.coroutines.flow.Flow

interface FixedScheduleRepository {
    val allSchedules: Flow<List<FixedScheduleEntity>>

    fun observeMonthlyExpectedExpense(yearMonth: String): Flow<Int>
    fun observeMonthlySpentFixedExpense(startInclusive: Long, endInclusive: Long): Flow<Int>

    suspend fun insertSchedule(schedule: FixedScheduleEntity): Long
    suspend fun updateSchedule(schedule: FixedScheduleEntity)
    suspend fun deleteSchedule(schedule: FixedScheduleEntity)
    suspend fun syncDueSchedules(nowEpochMillis: Long = System.currentTimeMillis())
}
