package com.jaehwan.moneybook.transaction.data.local

data class InstallmentPlanStatusSnapshot(
    val transactionId: Long,
    val totalCount: Int,
    val paidCount: Int,
    val remainingAmount: Int,
)
