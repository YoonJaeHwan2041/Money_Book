package com.jaehwan.moneybook.fixed.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaehwan.moneybook.category.domain.repository.CategoryRepository
import com.jaehwan.moneybook.fixed.data.local.FixedScheduleEntity
import com.jaehwan.moneybook.fixed.domain.repository.FixedScheduleRepository
import com.jaehwan.moneybook.transaction.domain.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FixedViewModel @Inject constructor(
    private val fixedScheduleRepository: FixedScheduleRepository,
    categoryRepository: CategoryRepository,
) : ViewModel() {

    val schedules: StateFlow<List<FixedScheduleRow>> = combine(
        fixedScheduleRepository.allSchedules,
        categoryRepository.allCategories,
    ) { schedules, categories ->
        val categoryMap = categories.associateBy { it.id }
        schedules.map { schedule ->
            FixedScheduleRow(
                schedule = schedule,
                categoryName = categoryMap[schedule.categoryId]?.name ?: "(알 수 없음)",
                categoryIconKey = categoryMap[schedule.categoryId]?.iconKey,
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList(),
    )

    fun observeMonthlyExpectedExpense(yearMonth: String): Flow<Int> =
        fixedScheduleRepository.observeMonthlyExpectedExpense(yearMonth)

    fun observeMonthlySpentExpense(startInclusive: Long, endInclusive: Long): Flow<Int> =
        fixedScheduleRepository.observeMonthlySpentFixedExpense(startInclusive, endInclusive)

    fun insertSchedule(
        kind: TransactionType,
        categoryId: Long,
        amount: Int,
        memo: String?,
        dayOfMonth: Int,
        triggerHour: Int = 14,
        startYearMonth: String,
    ) {
        if (!kind.isFixed) return
        viewModelScope.launch {
            fixedScheduleRepository.insertSchedule(
                FixedScheduleEntity(
                    kind = kind.key,
                    categoryId = categoryId,
                    amount = amount,
                    memo = memo,
                    dayOfMonth = dayOfMonth,
                    triggerHour = triggerHour,
                    startYearMonth = startYearMonth,
                )
            )
            fixedScheduleRepository.syncDueSchedules()
        }
    }

    fun updateSchedule(schedule: FixedScheduleEntity) {
        viewModelScope.launch {
            fixedScheduleRepository.updateSchedule(schedule)
            fixedScheduleRepository.syncDueSchedules()
        }
    }

    fun deleteSchedule(schedule: FixedScheduleEntity) {
        viewModelScope.launch {
            fixedScheduleRepository.deleteSchedule(schedule)
        }
    }

    fun syncDueSchedules() {
        viewModelScope.launch {
            fixedScheduleRepository.syncDueSchedules()
        }
    }
}

data class FixedScheduleRow(
    val schedule: FixedScheduleEntity,
    val categoryName: String,
    val categoryIconKey: String?,
)
