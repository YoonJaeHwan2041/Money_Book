package com.jaehwan.moneybook.transaction.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jaehwan.moneybook.report.data.local.InstallmentCategoryPaidRow
import kotlinx.coroutines.flow.Flow

@Dao
interface InstallmentDao {
    @Query("SELECT * FROM installment_plan")
    fun getAllPlans(): Flow<List<InstallmentPlanEntity>>

    @Query("SELECT * FROM installment_plan ORDER BY id ASC")
    suspend fun getAllPlansOnce(): List<InstallmentPlanEntity>

    @Query("SELECT * FROM installment_payment ORDER BY due_date ASC, sequence_no ASC")
    fun getAllPayments(): Flow<List<InstallmentPaymentEntity>>

    @Query("SELECT * FROM installment_payment ORDER BY id ASC")
    suspend fun getAllPaymentsOnce(): List<InstallmentPaymentEntity>

    @Query(
        """
        SELECT
            COALESCE(SUM(CASE WHEN is_paid = 0 THEN amount ELSE 0 END), 0) AS remainingTotal,
            COALESCE(COUNT(DISTINCT CASE WHEN is_paid = 0 THEN plan_id END), 0) AS activeCount
        FROM installment_payment
        """
    )
    fun getInstallmentSummary(): Flow<InstallmentSummarySnapshot>

    @Query(
        """
        SELECT
            ip.transaction_id AS transactionId,
            COUNT(p.id) AS totalCount,
            COALESCE(SUM(CASE WHEN p.is_paid = 1 THEN 1 ELSE 0 END), 0) AS paidCount,
            COALESCE(SUM(CASE WHEN p.is_paid = 0 THEN p.amount ELSE 0 END), 0) AS remainingAmount,
            COALESCE(SUM(CASE WHEN p.is_paid = 1 THEN p.amount ELSE 0 END), 0) AS paidAmount
        FROM installment_plan ip
        LEFT JOIN installment_payment p ON p.plan_id = ip.id
        GROUP BY ip.id
        """
    )
    fun getPlanStatusSnapshots(): Flow<List<InstallmentPlanStatusSnapshot>>

    @Query(
        """
        SELECT p.* FROM installment_payment p
        INNER JOIN installment_plan ip ON ip.id = p.plan_id
        WHERE ip.transaction_id = :transactionId
        ORDER BY p.sequence_no ASC
        """
    )
    suspend fun getPaymentsByTransactionId(transactionId: Long): List<InstallmentPaymentEntity>

    @Query(
        """
        SELECT p.* FROM installment_payment p
        INNER JOIN installment_plan ip ON ip.id = p.plan_id
        WHERE ip.transaction_id = :transactionId
        ORDER BY p.sequence_no ASC
        """
    )
    fun observePaymentsByTransactionId(transactionId: Long): Flow<List<InstallmentPaymentEntity>>

    @Query("SELECT * FROM installment_plan WHERE transaction_id = :transactionId LIMIT 1")
    suspend fun getPlanByTransactionId(transactionId: Long): InstallmentPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: InstallmentPlanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlans(plans: List<InstallmentPlanEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<InstallmentPaymentEntity>)

    @Update
    suspend fun updatePayment(payment: InstallmentPaymentEntity)

    @Query("DELETE FROM installment_plan WHERE transaction_id = :transactionId")
    suspend fun deletePlanByTransactionId(transactionId: Long)

    @Query("DELETE FROM installment_payment WHERE plan_id = :planId")
    suspend fun deletePaymentsByPlanId(planId: Long)

    @Query("DELETE FROM installment_payment")
    suspend fun deleteAllPayments()

    @Query("DELETE FROM installment_plan")
    suspend fun deleteAllPlans()

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM installment_payment
        WHERE is_paid = 0
          AND due_date BETWEEN :startInclusive AND :endInclusive
        """
    )
    fun observeExpectedInstallmentDue(startInclusive: Long, endInclusive: Long): Flow<Int>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM installment_payment
        WHERE is_paid = 1
        """
    )
    fun observeTotalPaidInstallmentAmount(): Flow<Int>

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM installment_payment
        WHERE is_paid = 1
          AND paid_at BETWEEN :startInclusive AND :endInclusive
        """
    )
    fun observeMonthlyPaidInstallmentTotal(startInclusive: Long, endInclusive: Long): Flow<Int>

    @Query(
        """
        SELECT
            t.category_id AS categoryId,
            c.name AS categoryName,
            c.icon_key AS categoryIconKey,
            COALESCE(SUM(p.amount), 0) AS amount
        FROM installment_payment p
        INNER JOIN installment_plan ip ON ip.id = p.plan_id
        INNER JOIN `transaction` t ON t.id = ip.transaction_id
        INNER JOIN category c ON c.id = t.category_id
        WHERE p.is_paid = 1
          AND p.paid_at BETWEEN :startInclusive AND :endInclusive
        GROUP BY t.category_id
        ORDER BY amount DESC
        """
    )
    fun observeMonthlyPaidInstallmentByCategory(
        startInclusive: Long,
        endInclusive: Long,
    ): Flow<List<InstallmentCategoryPaidRow>>
}
