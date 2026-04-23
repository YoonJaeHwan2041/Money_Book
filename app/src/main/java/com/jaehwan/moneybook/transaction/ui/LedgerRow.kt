package com.jaehwan.moneybook.transaction.ui

import com.jaehwan.moneybook.splitmember.data.local.SplitMemberEntity
import com.jaehwan.moneybook.transaction.data.local.InstallmentPlanEntity
import com.jaehwan.moneybook.transaction.data.local.TransactionEntity

data class LedgerRow(
    val transaction: TransactionEntity,
    val categoryName: String,
    val categoryIconKey: String? = null,
    val splitMembers: List<SplitMemberEntity> = emptyList(),
    val installmentPlan: InstallmentPlanEntity? = null,
    val installmentTotalCount: Int = 0,
    val installmentPaidCount: Int = 0,
    val installmentRemainingAmount: Int = 0,
)
