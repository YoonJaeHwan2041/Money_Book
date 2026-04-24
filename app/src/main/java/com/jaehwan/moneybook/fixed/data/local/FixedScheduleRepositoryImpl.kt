package com.jaehwan.moneybook.fixed.data.local

import androidx.room.withTransaction
import com.jaehwan.moneybook.common.data.local.AppDatabase
import com.jaehwan.moneybook.fixed.domain.repository.FixedScheduleRepository
import com.jaehwan.moneybook.transaction.data.local.TransactionEntity
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FixedScheduleRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
) : FixedScheduleRepository {

    private val fixedDao get() = db.fixedScheduleDao()
    private val transactionDao get() = db.transactionDao()

    override val allSchedules: Flow<List<FixedScheduleEntity>> = fixedDao.getAllSchedules()

    override fun observeMonthlyExpectedExpense(yearMonth: String): Flow<Int> =
        fixedDao.observeMonthlyExpectedExpense(yearMonth)

    override fun observeMonthlySpentFixedExpense(startInclusive: Long, endInclusive: Long): Flow<Int> =
        transactionDao.observeMonthlyFixedExpenseSpent(startInclusive, endInclusive)

    override suspend fun insertSchedule(schedule: FixedScheduleEntity): Long =
        fixedDao.insertSchedule(schedule)

    override suspend fun updateSchedule(schedule: FixedScheduleEntity) {
        fixedDao.updateSchedule(schedule.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun deleteSchedule(schedule: FixedScheduleEntity) {
        fixedDao.deleteSchedule(schedule)
    }

    override suspend fun syncDueSchedules(nowEpochMillis: Long) {
        val nowAtSeoul = Instant.ofEpochMilli(nowEpochMillis).atZone(SEOUL_ZONE)
        val nowYearMonth = YearMonth.from(nowAtSeoul.toLocalDate())
        val activeSchedules = fixedDao.getActiveSchedules()
        if (activeSchedules.isEmpty()) return

        db.withTransaction {
            activeSchedules.forEach { schedule ->
                val startYm = parseYearMonth(schedule.startYearMonth) ?: return@forEach
                val endYm = parseYearMonth(schedule.endYearMonth)
                if (nowYearMonth < startYm) return@forEach
                val targetEndYm = minOf(nowYearMonth, endYm ?: nowYearMonth)

                var cursor = startYm
                var mutableSchedule = schedule
                while (cursor <= targetEndYm) {
                    val cursorText = cursor.format(YM_FORMATTER)
                    if (mutableSchedule.lastGeneratedYearMonth != null && mutableSchedule.lastGeneratedYearMonth >= cursorText) {
                        cursor = cursor.plusMonths(1)
                        continue
                    }
                    val dueAt = dueDateTimeFor(mutableSchedule, cursor)
                    if (dueAt.isAfter(nowAtSeoul.toLocalDateTime())) break

                    val dueEpoch = dueAt.atZone(SEOUL_ZONE).toInstant().toEpochMilli()
                    val alreadyExists = transactionDao.countExactTransaction(
                        type = mutableSchedule.kind,
                        categoryId = mutableSchedule.categoryId,
                        amount = mutableSchedule.amount,
                        expectedDate = dueEpoch,
                        memo = mutableSchedule.memo,
                    ) > 0
                    if (!alreadyExists) {
                        transactionDao.insertTransaction(
                            TransactionEntity(
                                categoryId = mutableSchedule.categoryId,
                                amount = mutableSchedule.amount,
                                type = mutableSchedule.kind,
                                isConfirmed = true,
                                expectedDate = dueEpoch,
                                hasAlarm = false,
                                memo = mutableSchedule.memo,
                            )
                        )
                    }
                    mutableSchedule = mutableSchedule.copy(
                        lastGeneratedYearMonth = cursorText,
                        updatedAt = System.currentTimeMillis(),
                    )
                    fixedDao.updateSchedule(mutableSchedule)
                    cursor = cursor.plusMonths(1)
                }
            }
        }
    }

    private fun dueDateTimeFor(schedule: FixedScheduleEntity, yearMonth: YearMonth): LocalDateTime {
        val safeDay = schedule.dayOfMonth.coerceAtMost(yearMonth.lengthOfMonth()).coerceAtLeast(1)
        val date = LocalDate.of(yearMonth.year, yearMonth.monthValue, safeDay)
        val hour = schedule.triggerHour.coerceIn(0, 23)
        return date.atTime(hour, 0)
    }

    private fun parseYearMonth(value: String?): YearMonth? {
        if (value.isNullOrBlank()) return null
        return runCatching { YearMonth.parse(value, YM_FORMATTER) }.getOrNull()
    }

    private companion object {
        val SEOUL_ZONE: ZoneId = ZoneId.of("Asia/Seoul")
        val YM_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
    }
}
