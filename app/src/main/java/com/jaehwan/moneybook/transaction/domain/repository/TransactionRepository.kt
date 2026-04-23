package com.jaehwan.moneybook.transaction.domain.repository

import com.jaehwan.moneybook.splitmember.data.local.SplitMemberEntity
import com.jaehwan.moneybook.transaction.data.local.InstallmentPaymentEntity
import com.jaehwan.moneybook.transaction.data.local.InstallmentPlanStatusSnapshot
import com.jaehwan.moneybook.transaction.data.local.InstallmentPlanEntity
import com.jaehwan.moneybook.transaction.data.local.InstallmentSummarySnapshot
import com.jaehwan.moneybook.transaction.data.local.TransactionEntity
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    val allTransactions: Flow<List<TransactionEntity>>

    val allSplitMembers: Flow<List<SplitMemberEntity>>
    val allInstallmentPlans: Flow<List<InstallmentPlanEntity>>
    val installmentSummary: Flow<InstallmentSummarySnapshot>
    val installmentPlanStatuses: Flow<List<InstallmentPlanStatusSnapshot>>

    suspend fun insertTransaction(transaction: TransactionEntity): Long

    suspend fun updateTransaction(transaction: TransactionEntity)

    suspend fun deleteTransaction(transaction: TransactionEntity)

    suspend fun deleteTransactions(transactions: List<TransactionEntity>)

    suspend fun getSplitMembers(transactionId: Long): List<SplitMemberEntity>

    suspend fun insertSplit(transaction: TransactionEntity, members: List<SplitMemberEntity>): Long

    suspend fun updateSplit(transaction: TransactionEntity, members: List<SplitMemberEntity>)

    suspend fun updateSplitMember(member: SplitMemberEntity)

    suspend fun upsertInstallmentPlan(
        transactionId: Long,
        totalAmount: Int,
        months: Int,
        startDate: Long,
    )

    suspend fun clearInstallmentPlan(transactionId: Long)

    suspend fun updateInstallmentPayment(payment: InstallmentPaymentEntity)

    suspend fun getInstallmentPaymentsByTransaction(transactionId: Long): List<InstallmentPaymentEntity>

    suspend fun ensureMarchDemoTransactions()
}
