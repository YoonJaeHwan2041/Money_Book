package com.jaehwan.moneybook.transaction.data.local

import androidx.room.withTransaction
import com.jaehwan.moneybook.common.data.local.AppDatabase
import com.jaehwan.moneybook.splitmember.data.local.SplitMemberEntity
import com.jaehwan.moneybook.transaction.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
) : TransactionRepository {

    private val transactionDao get() = db.transactionDao()
    private val splitMemberDao get() = db.splitMemberDao()
    private val categoryDao get() = db.categoryDao()

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

    override suspend fun ensureMarchDemoTransactions() {
        if (transactionDao.countTransactions() > 0) return

        val incomeCategoryId = categoryDao.getCategoryByName("월급")?.id
            ?: categoryDao.getCategoryByName("유흥")?.id
            ?: categoryDao.getCategoryByName("교통")?.id
            ?: return
        val expenseCategoryId = categoryDao.getCategoryByName("외식")?.id
            ?: categoryDao.getCategoryByName("편의점")?.id
            ?: categoryDao.getCategoryByName("주거/공과금")?.id
            ?: return

        val year = LocalDate.now().year
        val zone = ZoneId.systemDefault()
        val marchDays = listOf(5, 12, 20, 7, 15, 24)
        val marchDates = marchDays.map { day ->
            LocalDate.of(year, 3, day).atStartOfDay(zone).toInstant().toEpochMilli()
        }
        val now = System.currentTimeMillis()

        val seedTx = listOf(
            TransactionEntity(
                categoryId = incomeCategoryId,
                amount = 3_200_000,
                type = "INCOME",
                isConfirmed = true,
                expectedDate = marchDates[0],
                hasAlarm = false,
                memo = "3월 월급",
                createdAt = now,
                updatedAt = now,
            ),
            TransactionEntity(
                categoryId = incomeCategoryId,
                amount = 150_000,
                type = "INCOME",
                isConfirmed = true,
                expectedDate = marchDates[1],
                hasAlarm = false,
                memo = "부수입",
                createdAt = now,
                updatedAt = now,
            ),
            TransactionEntity(
                categoryId = incomeCategoryId,
                amount = 200_000,
                type = "INCOME",
                isConfirmed = true,
                expectedDate = marchDates[2],
                hasAlarm = false,
                memo = "환급금",
                createdAt = now,
                updatedAt = now,
            ),
            TransactionEntity(
                categoryId = expenseCategoryId,
                amount = 85_000,
                type = "EXPENSE",
                isConfirmed = true,
                expectedDate = marchDates[3],
                hasAlarm = false,
                memo = "장보기",
                createdAt = now,
                updatedAt = now,
            ),
            TransactionEntity(
                categoryId = expenseCategoryId,
                amount = 45_000,
                type = "EXPENSE",
                isConfirmed = true,
                expectedDate = marchDates[4],
                hasAlarm = false,
                memo = "외식",
                createdAt = now,
                updatedAt = now,
            ),
            TransactionEntity(
                categoryId = expenseCategoryId,
                amount = 12_000,
                type = "EXPENSE",
                isConfirmed = true,
                expectedDate = marchDates[5],
                hasAlarm = false,
                memo = "교통비",
                createdAt = now,
                updatedAt = now,
            ),
        )

        db.withTransaction {
            if (transactionDao.countTransactions() == 0) {
                seedTx.forEach { tx -> transactionDao.insertTransaction(tx) }
            }
        }
    }
}
