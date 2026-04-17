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

    @Query("SELECT * FROM `transaction` WHERE created_at BETWEEN :start AND :end ORDER BY created_at DESC")
    fun getTransactionsByPeriod(start: Long, end: Long): Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM `transaction`")
    suspend fun countTransactions(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
}
