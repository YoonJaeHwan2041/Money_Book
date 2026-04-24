package com.jaehwan.moneybook.report.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaehwan.moneybook.report.domain.repository.ReportRepository
import com.jaehwan.moneybook.report.model.ReportCategoryDeltaItem
import com.jaehwan.moneybook.report.model.ReportCategorySpendItem
import com.jaehwan.moneybook.report.model.ReportForecast
import com.jaehwan.moneybook.report.model.ReportMonthCompare
import com.jaehwan.moneybook.report.model.ReportTopTransaction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val reportRepository: ReportRepository,
) : ViewModel() {
    private val _selectedMonth = MutableStateFlow(YearMonth.now(ZoneId.of("Asia/Seoul")))
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth

    val forecast: StateFlow<ReportForecast> = selectedMonth
        .flatMapLatest { month -> reportRepository.observeForecast(month) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportForecast())

    val categorySpending: StateFlow<List<ReportCategorySpendItem>> = selectedMonth
        .flatMapLatest { month -> reportRepository.observeCategorySpending(month) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topTransaction: StateFlow<ReportTopTransaction?> = selectedMonth
        .flatMapLatest { month -> reportRepository.observeTopTransaction(month) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val monthCompare: StateFlow<ReportMonthCompare> = selectedMonth
        .flatMapLatest { month -> reportRepository.observeMonthCompare(month) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportMonthCompare())

    val categoryCompare: StateFlow<List<ReportCategoryDeltaItem>> = selectedMonth
        .flatMapLatest { month -> reportRepository.observeCategoryCompare(month) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun changeMonth(month: YearMonth) {
        _selectedMonth.value = month
    }
}
