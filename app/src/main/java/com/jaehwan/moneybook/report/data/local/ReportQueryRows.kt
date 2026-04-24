package com.jaehwan.moneybook.report.data.local

data class ReportCategoryExpenseRow(
    val categoryId: Long,
    val categoryName: String,
    val categoryIconKey: String?,
    val amount: Int,
)

data class ReportTopTransactionRow(
    val transactionId: Long,
    val categoryName: String,
    val categoryIconKey: String?,
    val amount: Int,
    val type: String,
    val memo: String?,
    val expectedDate: Long,
)

data class InstallmentCategoryPaidRow(
    val categoryId: Long,
    val categoryName: String,
    val categoryIconKey: String?,
    val amount: Int,
)
