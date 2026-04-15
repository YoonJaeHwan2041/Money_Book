package com.jaehwan.moneybook.splitmember.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jaehwan.moneybook.transaction.data.local.TransactionEntity

@Entity(
    tableName = "split_member",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transaction_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["transaction_id"])]
)
data class SplitMemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "transaction_id")
    val transactionId: Long,

    @ColumnInfo(name = "member_name")
    val memberName: String,

    @ColumnInfo(name = "is_primary_payer")
    val isPrimaryPayer: Boolean = false,

    @ColumnInfo(name = "extra_amount")
    val extraAmount: Int = 0,

    @ColumnInfo(name = "deduction_amount")
    val deductionAmount: Int = 0,

    @ColumnInfo(name = "agreed_amount")
    val agreedAmount: Int? = null,

    @ColumnInfo(name = "is_paid")
    val isPaid: Boolean = false,

    @ColumnInfo(name = "payment_memo")
    val paymentMemo: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
)
