package com.jaehwan.moneybook.transaction.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface InstallmentDao {
    @Query("SELECT * FROM installment_plan")
    fun getAllPlans(): Flow<List<InstallmentPlanEntity>>

    @Query("SELECT * FROM installment_payment ORDER BY due_date ASC, sequence_no ASC")
    fun getAllPayments(): Flow<List<InstallmentPaymentEntity>>

    @Query(
        """
        SELECT p.* FROM installment_payment p
        INNER JOIN installment_plan ip ON ip.id = p.plan_id
        WHERE ip.transaction_id = :transactionId
        ORDER BY p.sequence_no ASC
        """
    )
    suspend fun getPaymentsByTransactionId(transactionId: Long): List<InstallmentPaymentEntity>

    @Query("SELECT * FROM installment_plan WHERE transaction_id = :transactionId LIMIT 1")
    suspend fun getPlanByTransactionId(transactionId: Long): InstallmentPlanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: InstallmentPlanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<InstallmentPaymentEntity>)

    @Update
    suspend fun updatePayment(payment: InstallmentPaymentEntity)

    @Query("DELETE FROM installment_plan WHERE transaction_id = :transactionId")
    suspend fun deletePlanByTransactionId(transactionId: Long)

    @Query("DELETE FROM installment_payment WHERE plan_id = :planId")
    suspend fun deletePaymentsByPlanId(planId: Long)
}
