package com.jaehwan.moneybook.transaction.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "installment_payment",
    foreignKeys = [
        ForeignKey(
            entity = InstallmentPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["plan_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(value = ["plan_id"]),
        Index(value = ["plan_id", "sequence_no"], unique = true),
    ],
)
data class InstallmentPaymentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "plan_id")
    val planId: Long,
    @ColumnInfo(name = "sequence_no")
    val sequenceNo: Int,
    @ColumnInfo(name = "due_date")
    val dueDate: Long,
    @ColumnInfo(name = "amount")
    val amount: Int,
    @ColumnInfo(name = "is_paid")
    val isPaid: Boolean = false,
    @ColumnInfo(name = "paid_at")
    val paidAt: Long? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
)
