package com.jaehwan.moneybook.fixed.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FixedScheduleDao {
    @Query("SELECT * FROM fixed_schedule ORDER BY day_of_month ASC, id DESC")
    fun getAllSchedules(): Flow<List<FixedScheduleEntity>>

    @Query("SELECT * FROM fixed_schedule ORDER BY id ASC")
    suspend fun getAllSchedulesOnce(): List<FixedScheduleEntity>

    @Query("SELECT * FROM fixed_schedule WHERE is_active = 1")
    suspend fun getActiveSchedules(): List<FixedScheduleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: FixedScheduleEntity): Long

    @Update
    suspend fun updateSchedule(schedule: FixedScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: FixedScheduleEntity)

    @Query("DELETE FROM fixed_schedule")
    suspend fun deleteAllSchedules()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSchedules(schedules: List<FixedScheduleEntity>)

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM fixed_schedule
        WHERE is_active = 1
        AND kind = 'FIXED_EXPENSE'
        AND start_year_month <= :yearMonth
        AND (end_year_month IS NULL OR end_year_month >= :yearMonth)
        """
    )
    fun observeMonthlyExpectedExpense(yearMonth: String): Flow<Int>
}
