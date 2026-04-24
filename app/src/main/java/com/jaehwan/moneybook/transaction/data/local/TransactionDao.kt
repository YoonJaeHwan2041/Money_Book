package com.jaehwan.moneybook.transaction.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.jaehwan.moneybook.report.data.local.ReportCategoryExpenseRow
import com.jaehwan.moneybook.report.data.local.ReportTopTransactionRow
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM `transaction` ORDER BY created_at DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM `transaction` ORDER BY id ASC")
    suspend fun getAllTransactionsOnce(): List<TransactionEntity>

    @Query("SELECT * FROM `transaction` WHERE created_at BETWEEN :start AND :end ORDER BY created_at DESC")
    fun getTransactionsByPeriod(start: Long, end: Long): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT COALESCE(SUM(
            CASE
                WHEN type IN ('EXPENSE', 'SPLIT') THEN amount
                WHEN type = 'FIXED_EXPENSE' AND is_confirmed = 1 THEN amount
                ELSE 0
            END
        ), 0)
        FROM `transaction`
        WHERE expected_date BETWEEN :startInclusive AND :endInclusive
        """
    )
    fun observeMonthlyExpenseTotal(startInclusive: Long, endInclusive: Long): Flow<Int>

    @Query(
        """
        SELECT
            t.category_id AS categoryId,
            c.name AS categoryName,
            c.icon_key AS categoryIconKey,
            COALESCE(SUM(t.amount), 0) AS amount
        FROM `transaction` t
        INNER JOIN category c ON c.id = t.category_id
        WHERE t.expected_date BETWEEN :startInclusive AND :endInclusive
          AND (
            t.type IN ('EXPENSE', 'SPLIT')
            OR (t.type = 'FIXED_EXPENSE' AND t.is_confirmed = 1)
          )
        GROUP BY t.category_id
        ORDER BY amount DESC
        """
    )
    fun observeMonthlyCategoryExpense(
        startInclusive: Long,
        endInclusive: Long,
    ): Flow<List<ReportCategoryExpenseRow>>

    @Query(
        """
        SELECT
            t.id AS transactionId,
            c.name AS categoryName,
            c.icon_key AS categoryIconKey,
            t.amount AS amount,
            t.type AS type,
            t.memo AS memo,
            t.expected_date AS expectedDate
        FROM `transaction` t
        INNER JOIN category c ON c.id = t.category_id
        WHERE t.expected_date BETWEEN :startInclusive AND :endInclusive
          AND (
            t.type IN ('EXPENSE', 'SPLIT')
            OR (t.type = 'FIXED_EXPENSE' AND t.is_confirmed = 1)
          )
        ORDER BY t.amount DESC, t.expected_date DESC
        LIMIT 1
        """
    )
    fun observeTopTransactionByMonth(
        startInclusive: Long,
        endInclusive: Long,
    ): Flow<ReportTopTransactionRow?>

    @Query("SELECT COUNT(*) FROM `transaction`")
    suspend fun countTransactions(): Int

    @Query(
        """
        SELECT COUNT(*) FROM `transaction`
        WHERE type = :type
          AND category_id = :categoryId
          AND amount = :amount
          AND expected_date = :expectedDate
          AND memo IS :memo
        """
    )
    suspend fun countExactTransaction(
        type: String,
        categoryId: Long,
        amount: Int,
        expectedDate: Long,
        memo: String?,
    ): Int

    @Query(
        """
        SELECT COALESCE(SUM(amount), 0) FROM `transaction`
        WHERE type = 'FIXED_EXPENSE'
          AND expected_date BETWEEN :startInclusive AND :endInclusive
        """
    )
    fun observeMonthlyFixedExpenseSpent(startInclusive: Long, endInclusive: Long): Flow<Int>

    @Query(
        """
        SELECT COALESCE(SUM(
            CASE
                WHEN type = 'INCOME' THEN amount
                WHEN type = 'EXPENSE' THEN -amount
                WHEN type = 'SPLIT' THEN -amount
                WHEN type = 'INSTALLMENT' THEN 0
                WHEN type = 'FIXED_INCOME' AND is_confirmed = 1 THEN amount
                WHEN type = 'FIXED_EXPENSE' AND is_confirmed = 1 THEN -amount
                ELSE 0
            END
        ), 0)
        FROM `transaction`
        """
    )
    fun observeCurrentBalanceBase(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)

    @Query("DELETE FROM `transaction`")
    suspend fun deleteAllTransactions()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTransactions(transactions: List<TransactionEntity>)

    @Query(
        """
        UPDATE `transaction`
        SET is_confirmed = 1, updated_at = :updatedAt
        WHERE id = :transactionId
          AND type IN (:fixedIncomeKey, :fixedExpenseKey)
        """
    )
    suspend fun confirmPendingFixedTransaction(
        transactionId: Long,
        updatedAt: Long = System.currentTimeMillis(),
        fixedIncomeKey: String = "FIXED_INCOME",
        fixedExpenseKey: String = "FIXED_EXPENSE",
    )

    @Query(
        """
        DELETE FROM `transaction`
        WHERE id = :transactionId
          AND is_confirmed = 0
          AND type IN (:fixedIncomeKey, :fixedExpenseKey)
        """
    )
    suspend fun deletePendingFixedTransaction(
        transactionId: Long,
        fixedIncomeKey: String = "FIXED_INCOME",
        fixedExpenseKey: String = "FIXED_EXPENSE",
    )
}
