package com.jaehwan.moneybook.transaction.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jaehwan.moneybook.category.domain.repository.CategoryRepository
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

    val ledgerRows: StateFlow<List<LedgerRow>> = combine(
        transactionRepository.allTransactions,
        categoryRepository.allCategories,
        transactionRepository.allSplitMembers,
    ) { transactions, categories, splitMembers ->
        val byId = categories.associateBy { it.id }
        val membersByTx = splitMembers.groupBy { it.transactionId }
        transactions.map { tx ->
            LedgerRow(
                transaction = tx,
                categoryName = byId[tx.categoryId]?.name ?: "(알 수 없음)",
                splitMembers = membersByTx[tx.id].orEmpty(),
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun insertTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionRepository.insertTransaction(transaction)
        }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionRepository.updateTransaction(transaction)
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            transactionRepository.deleteTransaction(transaction)
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
}
