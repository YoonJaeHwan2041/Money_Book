package com.jaehwan.moneybook.transaction.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaehwan.moneybook.category.domain.repository.CategoryRepository
import com.jaehwan.moneybook.transaction.data.local.InstallmentPaymentEntity
import com.jaehwan.moneybook.splitmember.data.local.SplitMemberEntity
import com.jaehwan.moneybook.transaction.data.local.TransactionEntity
import com.jaehwan.moneybook.transaction.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LedgerViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val categoryRepository: CategoryRepository,
) : ViewModel() {
    init {
        viewModelScope.launch {
            transactionRepository.ensureMarchDemoTransactions()
        }
    }

    val ledgerRows: StateFlow<List<LedgerRow>> = combine(
        transactionRepository.allTransactions,
        categoryRepository.allCategories,
        transactionRepository.allSplitMembers,
        transactionRepository.allInstallmentPlans,
        transactionRepository.allInstallmentPayments,
    ) { transactions, categories, splitMembers, installmentPlans, installmentPayments ->
        val byId = categories.associateBy { it.id }
        val membersByTx = splitMembers.groupBy { it.transactionId }
        val planByTxId = installmentPlans.associateBy { it.transactionId }
        val paymentsByPlanId = installmentPayments.groupBy { it.planId }
        transactions.map { tx ->
            val plan = planByTxId[tx.id]
            LedgerRow(
                transaction = tx,
                categoryName = byId[tx.categoryId]?.name ?: "(알 수 없음)",
                categoryIconKey = byId[tx.categoryId]?.iconKey,
                splitMembers = membersByTx[tx.id].orEmpty(),
                installmentPlan = plan,
                installmentPayments = plan?.let { paymentsByPlanId[it.id].orEmpty() }.orEmpty(),
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val installmentSummary: StateFlow<InstallmentSummary> = transactionRepository.allInstallmentPayments
        .combine(transactionRepository.allInstallmentPlans) { payments, plans ->
            val unpaidByPlan = payments
                .filter { !it.isPaid }
                .groupBy { it.planId }
            val remainingTotal = unpaidByPlan.values.flatten().sumOf { it.amount }
            val activeCount = plans.count { plan ->
                unpaidByPlan[plan.id].isNullOrEmpty().not()
            }
            InstallmentSummary(
                remainingTotal = remainingTotal,
                activeCount = activeCount,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InstallmentSummary(),
        )

    fun insertTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionRepository.insertTransaction(transaction)
        }
    }

    fun insertTransactionWithInstallment(
        transaction: TransactionEntity,
        installmentTotalAmount: Int,
        installmentMonths: Int,
        installmentStartDate: Long,
    ) {
        viewModelScope.launch {
            val txId = transactionRepository.insertTransaction(transaction)
            transactionRepository.upsertInstallmentPlan(
                transactionId = txId,
                totalAmount = installmentTotalAmount,
                months = installmentMonths,
                startDate = installmentStartDate,
            )
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionRepository.updateTransaction(transaction)
        }
    }

    fun updateTransactionWithInstallment(
        transaction: TransactionEntity,
        installmentTotalAmount: Int,
        installmentMonths: Int,
        installmentStartDate: Long,
    ) {
        viewModelScope.launch {
            transactionRepository.updateTransaction(transaction)
            transactionRepository.upsertInstallmentPlan(
                transactionId = transaction.id,
                totalAmount = installmentTotalAmount,
                months = installmentMonths,
                startDate = installmentStartDate,
            )
        }
    }

    fun clearInstallment(transactionId: Long) {
        viewModelScope.launch {
            transactionRepository.clearInstallmentPlan(transactionId)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
        }
    }

    fun deleteTransactions(transactions: List<TransactionEntity>) {
        if (transactions.isEmpty()) return
        viewModelScope.launch {
            transactionRepository.deleteTransactions(transactions)
        }
    }

    fun insertSplit(transaction: TransactionEntity, members: List<SplitMemberEntity>) {
        viewModelScope.launch {
            transactionRepository.insertSplit(transaction, members)
        }
    }

    fun updateSplit(transaction: TransactionEntity, members: List<SplitMemberEntity>) {
        viewModelScope.launch {
            transactionRepository.updateSplit(transaction, members)
        }
    }

    suspend fun getSplitMembers(transactionId: Long): List<SplitMemberEntity> =
        transactionRepository.getSplitMembers(transactionId)

    fun updateSplitMember(member: SplitMemberEntity) {
        viewModelScope.launch {
            transactionRepository.updateSplitMember(member)
        }
    }

    fun updateInstallmentPayment(payment: InstallmentPaymentEntity) {
        viewModelScope.launch {
            transactionRepository.updateInstallmentPayment(payment)
        }
    }

    suspend fun getInstallmentPayments(transactionId: Long): List<InstallmentPaymentEntity> =
        transactionRepository.getInstallmentPaymentsByTransaction(transactionId)
}
