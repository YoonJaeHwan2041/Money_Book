package com.jaehwan.moneybook.transaction.data.local

import androidx.room.withTransaction
import com.jaehwan.moneybook.common.data.local.AppDatabase
import com.jaehwan.moneybook.splitmember.data.local.SplitMemberEntity
import com.jaehwan.moneybook.transaction.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
) : TransactionRepository {

    private val transactionDao get() = db.transactionDao()
    private val splitMemberDao get() = db.splitMemberDao()

    override val allTransactions: Flow<List<TransactionEntity>> =
        transactionDao.getAllTransactions()

    override val allSplitMembers: Flow<List<SplitMemberEntity>> =
        splitMemberDao.getAllMembers()

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

    override suspend fun getSplitMembers(transactionId: Long): List<SplitMemberEntity> =
        splitMemberDao.getMembersByTransactionId(transactionId)

    override suspend fun insertSplit(
        transaction: TransactionEntity,
        members: List<SplitMemberEntity>,
    ): Long = db.withTransaction {
        val newId = transactionDao.insertTransaction(transaction)
        val now = System.currentTimeMillis()
        val withTx = members.map { m ->
            m.copy(
                transactionId = newId,
                id = 0,
                createdAt = now,
                updatedAt = now,
            )
        }
        splitMemberDao.insertMembers(withTx)
        newId
    }

    override suspend fun updateSplit(
        transaction: TransactionEntity,
        members: List<SplitMemberEntity>,
    ) {
        db.withTransaction {
            transactionDao.updateTransaction(
                transaction.copy(updatedAt = System.currentTimeMillis())
            )
            splitMemberDao.deleteByTransactionId(transaction.id)
            val now = System.currentTimeMillis()
            splitMemberDao.insertMembers(
                members.map { m ->
                    m.copy(
                        transactionId = transaction.id,
                        id = 0,
                        createdAt = now,
                        updatedAt = now,
                    )
                }
            )
        }
    }

    override suspend fun updateSplitMember(member: SplitMemberEntity) {
        splitMemberDao.updateMember(
            member.copy(updatedAt = System.currentTimeMillis())
        )
    }
}
