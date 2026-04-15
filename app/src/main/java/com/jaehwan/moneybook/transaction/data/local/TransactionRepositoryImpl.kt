package com.jaehwan.moneybook.transaction.data.local

import com.jaehwan.moneybook.transaction.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
) : TransactionRepository {

    override val allTransactions: Flow<List<TransactionEntity>> =
        transactionDao.getAllTransactions()

    override suspend fun insertTransaction(transaction: TransactionEntity): Long =
        transactionDao.insertTransaction(transaction)

    override suspend fun updateTransaction(transaction: TransactionEntity) {
        transactionDao.updateTransaction(
            transaction.copy(updatedAt = System.currentTimeMillis())
        )
    }

    override suspend fun deleteTransaction(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }
}
