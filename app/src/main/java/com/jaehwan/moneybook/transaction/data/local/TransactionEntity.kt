package com.jaehwan.moneybook.transaction.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jaehwan.moneybook.category.data.local.CategoryEntity

@Entity(
    tableName = "transaction",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,       // 부모 엔티티 클래스
            parentColumns = ["id"],              // 부모 테이블의 PK 컬럼명
            childColumns = ["category_id"],      // 현재 테이블의 FK 컬럼명
            onDelete = ForeignKey.Companion.CASCADE        // 부모 삭제 시 동작 (CASCADE, SET_NULL 등)
        )
    ],
    indices = [Index(value = ["category_id"])]   // FK 컬럼은 인덱스 설정을 권장 (성능 최적화)
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "category_id")
    val categoryId: Long,

    @ColumnInfo(name = "amount")
    val amount: Int,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "is_confirmed")
    val isConfirmed: Boolean,

    @ColumnInfo(name = "expected_date")
    val expectedDate: Long,

    @ColumnInfo(name = "has_alarm")
    val hasAlarm: Boolean,

    @ColumnInfo(name = "memo")
    val memo: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)