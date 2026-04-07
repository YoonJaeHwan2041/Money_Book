package com.jaehwan.moneybook.splitmember.data.local

import androidx.room.*
import com.jaehwan.moneybook.transaction.data.local.TransactionEntity

@Entity(
    tableName = "split_member",
    foreignKeys = [
        ForeignKey(
            // 2. 이제 클래스 이름만 깔끔하게 쓸 수 있습니다!
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

    @ColumnInfo(name = "is_paid")
    val isPaid: Boolean = false,

    @ColumnInfo(name = "payment_memo")
    val paymentMemo: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()




)
