package com.jaehwan.moneybook.report.data.local

import com.jaehwan.moneybook.common.data.local.AppDatabase
import com.jaehwan.moneybook.report.domain.repository.ReportRepository
import com.jaehwan.moneybook.report.model.ReportCategoryDeltaItem
import com.jaehwan.moneybook.report.model.ReportCategorySpendItem
import com.jaehwan.moneybook.report.model.ReportForecast
import com.jaehwan.moneybook.report.model.ReportMonthCompare
import com.jaehwan.moneybook.report.model.ReportTopTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepositoryImpl @Inject constructor(
    db: AppDatabase,
) : ReportRepository {
    private val transactionDao = db.transactionDao()
    private val fixedDao = db.fixedScheduleDao()
    private val installmentDao = db.installmentDao()

    override fun observeCategorySpending(month: YearMonth): Flow<List<ReportCategorySpendItem>> {
        val range = monthRange(month)
        return combine(
            transactionDao.observeMonthlyCategoryExpense(range.start, range.end),
            installmentDao.observeMonthlyPaidInstallmentByCategory(range.start, range.end),
            transactionDao.observeMonthlyExpenseTotal(range.start, range.end),
            installmentDao.observeMonthlyPaidInstallmentTotal(range.start, range.end),
        ) { rows, installmentRows, txTotal, installmentTotal ->
            val merged = mutableMapOf<Long, ReportCategorySpendItem>()
            rows.forEach { row ->
                merged[row.categoryId] = ReportCategorySpendItem(
                    categoryId = row.categoryId,
                    name = row.categoryName,
                    iconKey = row.categoryIconKey,
                    amount = row.amount,
                    ratio = 0f,
                )
            }
            installmentRows.forEach { row ->
                val current = merged[row.categoryId]
                if (current == null) {
                    merged[row.categoryId] = ReportCategorySpendItem(
                        categoryId = row.categoryId,
                        name = row.categoryName,
                        iconKey = row.categoryIconKey,
                        amount = row.amount,
                        ratio = 0f,
                    )
                } else {
                    merged[row.categoryId] = current.copy(amount = current.amount + row.amount)
                }
            }
            val total = txTotal + installmentTotal
            val safeTotal = total.coerceAtLeast(1)
            merged.values
                .map { item ->
                    item.copy(
                        ratio = (item.amount.toFloat() / safeTotal.toFloat()) * 100f,
                    )
                }
                .sortedByDescending { it.amount }
        }
    }

    override fun observeTopTransaction(month: YearMonth): Flow<ReportTopTransaction?> {
        val range = monthRange(month)
        return transactionDao.observeTopTransactionByMonth(range.start, range.end)
            .map { row ->
                row ?: return@map null
                ReportTopTransaction(
                    transactionId = row.transactionId,
                    title = row.memo?.takeIf { it.isNotBlank() } ?: row.categoryName,
                    categoryName = row.categoryName,
                    categoryIconKey = row.categoryIconKey,
                    amount = row.amount,
                    type = row.type,
                    expectedDate = row.expectedDate,
                )
            }
    }

    override fun observeMonthCompare(month: YearMonth): Flow<ReportMonthCompare> {
        val currentRange = monthRange(month)
        val prevRange = monthRange(month.minusMonths(1))
        return combine(
            transactionDao.observeMonthlyExpenseTotal(currentRange.start, currentRange.end),
            transactionDao.observeMonthlyExpenseTotal(prevRange.start, prevRange.end),
        ) { current, previous ->
            ReportMonthCompare(
                currentExpense = current,
                previousExpense = previous,
            )
        }
    }

    override fun observeCategoryCompare(month: YearMonth): Flow<List<ReportCategoryDeltaItem>> {
        val currentRange = monthRange(month)
        val prevRange = monthRange(month.minusMonths(1))
        return combine(
            transactionDao.observeMonthlyCategoryExpense(currentRange.start, currentRange.end),
            transactionDao.observeMonthlyCategoryExpense(prevRange.start, prevRange.end),
        ) { currentRows, previousRows ->
            val previousByCategory = previousRows.associateBy { it.categoryId }
            val mergedIds = (currentRows.map { it.categoryId } + previousRows.map { it.categoryId }).distinct()
            mergedIds.map { categoryId ->
                val current = currentRows.firstOrNull { it.categoryId == categoryId }
                val previous = previousByCategory[categoryId]
                ReportCategoryDeltaItem(
                    categoryId = categoryId,
                    name = current?.categoryName ?: previous?.categoryName ?: "(알 수 없음)",
                    iconKey = current?.categoryIconKey ?: previous?.categoryIconKey,
                    currentAmount = current?.amount ?: 0,
                    previousAmount = previous?.amount ?: 0,
                )
            }.sortedByDescending { kotlin.math.abs(it.delta) }
        }
    }

    override fun observeForecast(baseMonth: YearMonth): Flow<ReportForecast> {
        val nextMonth = baseMonth.plusMonths(1)
        val nextRange = monthRange(nextMonth)
        val nextYm = nextMonth.toYmText()
        return combine(
            transactionDao.observeCurrentBalanceBase(),
            installmentDao.observeTotalPaidInstallmentAmount(),
            fixedDao.observeMonthlyExpectedByKind(nextYm, "FIXED_INCOME"),
            fixedDao.observeMonthlyExpectedByKind(nextYm, "FIXED_EXPENSE"),
            installmentDao.observeExpectedInstallmentDue(nextRange.start, nextRange.end),
        ) { currentBaseBalance, paidInstallmentAmount, nextFixedIncome, nextFixedExpense, nextInstallmentDue ->
            ReportForecast(
                currentBalance = currentBaseBalance - paidInstallmentAmount,
                nextMonthFixedIncome = nextFixedIncome,
                nextMonthFixedExpense = nextFixedExpense,
                nextMonthInstallmentDue = nextInstallmentDue,
            )
        }
    }

    private fun monthRange(month: YearMonth): TimeRange {
        val zone = ZoneId.of("Asia/Seoul")
        val start = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val end = month.atEndOfMonth().atTime(23, 59, 59).atZone(zone).toInstant().toEpochMilli()
        return TimeRange(start, end)
    }

    private fun YearMonth.toYmText(): String = "%04d-%02d".format(year, monthValue)

    private data class TimeRange(
        val start: Long,
        val end: Long,
    )
}
