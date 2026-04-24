package com.jaehwan.moneybook.report.model

data class ReportCategorySpendItem(
    val categoryId: Long,
    val name: String,
    val iconKey: String?,
    val amount: Int,
    val ratio: Float,
)

data class ReportTopTransaction(
    val transactionId: Long,
    val title: String,
    val categoryName: String,
    val categoryIconKey: String?,
    val amount: Int,
    val type: String,
    val expectedDate: Long,
)

data class ReportMonthCompare(
    val currentExpense: Int = 0,
    val previousExpense: Int = 0,
) {
    val delta: Int get() = currentExpense - previousExpense
    val deltaRatio: Float
        get() = if (previousExpense == 0) 0f else (delta.toFloat() / previousExpense.toFloat()) * 100f
}

data class ReportCategoryDeltaItem(
    val categoryId: Long,
    val name: String,
    val iconKey: String?,
    val currentAmount: Int,
    val previousAmount: Int,
) {
    val delta: Int get() = currentAmount - previousAmount
}

data class ReportForecast(
    val currentBalance: Int = 0,
    val nextMonthFixedIncome: Int = 0,
    val nextMonthFixedExpense: Int = 0,
    val nextMonthInstallmentDue: Int = 0,
) {
    val forecastAmount: Int
        get() = currentBalance + nextMonthFixedIncome - (nextMonthFixedExpense + nextMonthInstallmentDue)
}
