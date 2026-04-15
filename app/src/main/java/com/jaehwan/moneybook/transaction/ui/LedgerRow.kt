package com.jaehwan.moneybook.transaction.ui

import com.jaehwan.moneybook.splitmember.data.local.SplitMemberEntity
import com.jaehwan.moneybook.transaction.data.local.TransactionEntity

data class LedgerRow(
    val transaction: TransactionEntity,
    val categoryName: String,
    val splitMembers: List<SplitMemberEntity> = emptyList(),
)
