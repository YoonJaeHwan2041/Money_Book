package com.jaehwan.moneybook.transaction.data.local

import androidx.room.withTransaction
import com.jaehwan.moneybook.common.data.local.AppDatabase
import com.jaehwan.moneybook.splitmember.data.local.SplitMemberEntity
import com.jaehwan.moneybook.transaction.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val db: AppDatabase,
) : TransactionRepository {

    private val transactionDao get() = db.transactionDao()
    private val splitMemberDao get() = db.splitMemberDao()
    private val installmentDao get() = db.installmentDao()
    private val categoryDao get() = db.categoryDao()

    override val allTransactions: Flow<List<TransactionEntity>> =
        transactionDao.getAllTransactions()

    override val allSplitMembers: Flow<List<SplitMemberEntity>> =
        splitMemberDao.getAllMembers()

    override val allInstallmentPlans: Flow<List<InstallmentPlanEntity>> =
        installmentDao.getAllPlans()

    override val installmentSummary: Flow<InstallmentSummarySnapshot> =
        installmentDao.getInstallmentSummary()

    override val installmentPlanStatuses: Flow<List<InstallmentPlanStatusSnapshot>> =
        installmentDao.getPlanStatusSnapshots()

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

    override suspend fun deleteTransactions(transactions: List<TransactionEntity>) {
        if (transactions.isEmpty()) return
        // SplitMemberEntity FK는 CASCADE라 거래 삭제 시 멤버 행도 함께 제거됨.
        db.withTransaction {
            transactions.forEach { transactionDao.deleteTransaction(it) }
        }
    }

    override suspend fun confirmFixedTransaction(transactionId: Long) {
        transactionDao.confirmPendingFixedTransaction(transactionId)
    }

    override suspend fun discardPendingFixedTransaction(transactionId: Long) {
        transactionDao.deletePendingFixedTransaction(transactionId)
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

    override suspend fun upsertInstallmentPlan(
        transactionId: Long,
        totalAmount: Int,
        months: Int,
        startDate: Long,
    ) {
        db.withTransaction {
            val now = System.currentTimeMillis()
            val existing = installmentDao.getPlanByTransactionId(transactionId)
            if (existing != null) {
                installmentDao.deletePaymentsByPlanId(existing.id)
                installmentDao.deletePlanByTransactionId(transactionId)
            }
            val planId = installmentDao.insertPlan(
                InstallmentPlanEntity(
                    transactionId = transactionId,
                    totalAmount = totalAmount,
                    months = months,
                    startDate = startDate,
                    createdAt = now,
                    updatedAt = now,
                )
            )
            installmentDao.insertPayments(
                buildInstallmentPayments(
                    planId = planId,
                    totalAmount = totalAmount,
                    months = months,
                    startDate = startDate,
                    now = now,
                )
            )
        }
    }

    override suspend fun clearInstallmentPlan(transactionId: Long) {
        installmentDao.deletePlanByTransactionId(transactionId)
    }

    override suspend fun updateInstallmentPayment(payment: InstallmentPaymentEntity) {
        installmentDao.updatePayment(
            payment.copy(updatedAt = System.currentTimeMillis())
        )
    }

    override suspend fun getInstallmentPaymentsByTransaction(transactionId: Long): List<InstallmentPaymentEntity> =
        installmentDao.getPaymentsByTransactionId(transactionId)

    override fun observeInstallmentPaymentsByTransaction(transactionId: Long): Flow<List<InstallmentPaymentEntity>> =
        installmentDao.observePaymentsByTransactionId(transactionId)

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

    private fun buildInstallmentPayments(
        planId: Long,
        totalAmount: Int,
        months: Int,
        startDate: Long,
        now: Long,
    ): List<InstallmentPaymentEntity> {
        val safeMonths = months.coerceAtLeast(1)
        val amountPerMonth = totalAmount / safeMonths
        val remainder = totalAmount % safeMonths
        val zone = ZoneId.systemDefault()
        val start = ZonedDateTime.ofInstant(
            java.time.Instant.ofEpochMilli(startDate),
            zone,
        )
        return (1..safeMonths).map { seq ->
            val plusMonth = start.plusMonths((seq - 1).toLong())
            val dueDate = plusMonth.toInstant().toEpochMilli()
            val amount = amountPerMonth + if (seq <= remainder) 1 else 0
            InstallmentPaymentEntity(
                planId = planId,
                sequenceNo = seq,
                dueDate = dueDate,
                amount = amount,
                isPaid = false,
                paidAt = null,
                createdAt = now,
                updatedAt = now,
            )
        }
    }
}
