package com.jaehwan.moneybook.report.domain.repository

import com.jaehwan.moneybook.report.model.ReportCategoryDeltaItem
import com.jaehwan.moneybook.report.model.ReportCategorySpendItem
import com.jaehwan.moneybook.report.model.ReportForecast
import com.jaehwan.moneybook.report.model.ReportMonthCompare
import com.jaehwan.moneybook.report.model.ReportTopTransaction
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface ReportRepository {
    fun observeCategorySpending(month: YearMonth): Flow<List<ReportCategorySpendItem>>
    fun observeTopTransaction(month: YearMonth): Flow<ReportTopTransaction?>
    fun observeMonthCompare(month: YearMonth): Flow<ReportMonthCompare>
    fun observeCategoryCompare(month: YearMonth): Flow<List<ReportCategoryDeltaItem>>
    fun observeForecast(baseMonth: YearMonth): Flow<ReportForecast>
}
