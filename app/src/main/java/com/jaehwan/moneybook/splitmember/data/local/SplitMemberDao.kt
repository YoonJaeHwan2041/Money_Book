package com.jaehwan.moneybook.splitmember.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface SplitMemberDao {

    @Query("SELECT * FROM split_member WHERE transaction_id = :transactionId")
    fun getMemberByTransactionId(transactionId: String): List<SplitMemberEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemebers(members: List<SplitMemberEntity>)

    @Update
    suspend fun updateMember(member: SplitMemberEntity)

    @Delete
    suspend fun deleteMember(member: SplitMemberEntity)
}