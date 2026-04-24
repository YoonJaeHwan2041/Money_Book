package com.jaehwan.moneybook.transaction.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query("SELECT * FROM `transaction` ORDER BY created_at DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM `transaction` ORDER BY id ASC")
    suspend fun getAllTransactionsOnce(): List<TransactionEntity>

    @Query("SELECT * FROM `transaction` WHERE created_at BETWEEN :start AND :end ORDER BY created_at DESC")
    fun getTransactionsByPeriod(start: Long, end: Long): Flow<List<TransactionEntity>>

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
}
