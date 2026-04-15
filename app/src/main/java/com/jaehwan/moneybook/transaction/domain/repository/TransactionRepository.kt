package com.jaehwan.moneybook.transaction.domain.repository

import com.jaehwan.moneybook.transaction.data.local.TransactionEntity
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    val allTransactions: Flow<List<TransactionEntity>>

    suspend fun insertTransaction(transaction: TransactionEntity): Long

    suspend fun updateTransaction(transaction: TransactionEntity)

    suspend fun deleteTransaction(transaction: TransactionEntity)
}
