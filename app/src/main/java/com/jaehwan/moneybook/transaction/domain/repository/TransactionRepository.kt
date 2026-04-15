package com.jaehwan.moneybook.transaction.domain.repository

import com.jaehwan.moneybook.splitmember.data.local.SplitMemberEntity
import com.jaehwan.moneybook.transaction.data.local.TransactionEntity
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    val allTransactions: Flow<List<TransactionEntity>>

    val allSplitMembers: Flow<List<SplitMemberEntity>>

    suspend fun insertTransaction(transaction: TransactionEntity): Long

    suspend fun updateTransaction(transaction: TransactionEntity)

    suspend fun deleteTransaction(transaction: TransactionEntity)

    suspend fun getSplitMembers(transactionId: Long): List<SplitMemberEntity>

    suspend fun insertSplit(transaction: TransactionEntity, members: List<SplitMemberEntity>): Long

    suspend fun updateSplit(transaction: TransactionEntity, members: List<SplitMemberEntity>)

    suspend fun updateSplitMember(member: SplitMemberEntity)
}
