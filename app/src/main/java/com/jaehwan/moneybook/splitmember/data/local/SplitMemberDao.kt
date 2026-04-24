package com.jaehwan.moneybook.splitmember.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SplitMemberDao {

    @Query("SELECT * FROM split_member ORDER BY transaction_id, is_primary_payer DESC, id ASC")
    fun getAllMembers(): Flow<List<SplitMemberEntity>>

    @Query("SELECT * FROM split_member ORDER BY id ASC")
    suspend fun getAllMembersOnce(): List<SplitMemberEntity>

    @Query("SELECT * FROM split_member WHERE transaction_id = :transactionId ORDER BY is_primary_payer DESC, id ASC")
    fun observeMembers(transactionId: Long): Flow<List<SplitMemberEntity>>

    @Query("SELECT * FROM split_member WHERE transaction_id = :transactionId ORDER BY is_primary_payer DESC, id ASC")
    suspend fun getMembersByTransactionId(transactionId: Long): List<SplitMemberEntity>

    @Query("DELETE FROM split_member WHERE transaction_id = :transactionId")
    suspend fun deleteByTransactionId(transactionId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<SplitMemberEntity>)

    @Update
    suspend fun updateMember(member: SplitMemberEntity)

    @Delete
    suspend fun deleteMember(member: SplitMemberEntity)

    @Query("DELETE FROM split_member")
    suspend fun deleteAllMembers()
}
