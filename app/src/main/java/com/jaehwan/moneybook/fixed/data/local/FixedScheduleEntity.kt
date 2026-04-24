package com.jaehwan.moneybook.fixed.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.jaehwan.moneybook.category.data.local.CategoryEntity

@Entity(
    tableName = "fixed_schedule",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [
        Index(value = ["category_id"]),
        Index(value = ["is_active"]),
        Index(value = ["kind"]),
    ],
)
data class FixedScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "kind")
    val kind: String,
    @ColumnInfo(name = "category_id")
    val categoryId: Long,
    @ColumnInfo(name = "amount")
    val amount: Int,
    @ColumnInfo(name = "memo")
    val memo: String? = null,
    @ColumnInfo(name = "day_of_month")
    val dayOfMonth: Int,
    @ColumnInfo(name = "trigger_hour")
    val triggerHour: Int = 14,
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    @ColumnInfo(name = "start_year_month")
    val startYearMonth: String,
    @ColumnInfo(name = "end_year_month")
    val endYearMonth: String? = null,
    @ColumnInfo(name = "last_generated_year_month")
    val lastGeneratedYearMonth: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
)
