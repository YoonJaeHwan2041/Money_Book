package com.jaehwan.moneybook.common.data.local

import androidx.room.Database
import androidx.room.migration.Migration
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.jaehwan.moneybook.category.data.local.CategoryDao
import com.jaehwan.moneybook.category.data.local.CategoryEntity
import com.jaehwan.moneybook.fixed.data.local.FixedScheduleDao
import com.jaehwan.moneybook.fixed.data.local.FixedScheduleEntity
import com.jaehwan.moneybook.splitmember.data.local.SplitMemberDao
import com.jaehwan.moneybook.splitmember.data.local.SplitMemberEntity
import com.jaehwan.moneybook.transaction.data.local.InstallmentDao
import com.jaehwan.moneybook.transaction.data.local.InstallmentPaymentEntity
import com.jaehwan.moneybook.transaction.data.local.InstallmentPlanEntity
import com.jaehwan.moneybook.transaction.data.local.TransactionDao
import com.jaehwan.moneybook.transaction.data.local.TransactionEntity

@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        SplitMemberEntity::class,
        InstallmentPlanEntity::class,
        InstallmentPaymentEntity::class,
        FixedScheduleEntity::class,
    ],
    version = 5,
    exportSchema = false // 테스트 용도가 아니라면 false로 둡니다.
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun splitMemberDao(): SplitMemberDao
    abstract fun installmentDao(): InstallmentDao
    abstract fun fixedScheduleDao(): FixedScheduleDao

    companion object {
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `fixed_schedule` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `kind` TEXT NOT NULL,
                        `category_id` INTEGER NOT NULL,
                        `amount` INTEGER NOT NULL,
                        `memo` TEXT,
                        `day_of_month` INTEGER NOT NULL,
                        `trigger_hour` INTEGER NOT NULL,
                        `is_active` INTEGER NOT NULL,
                        `start_year_month` TEXT NOT NULL,
                        `end_year_month` TEXT,
                        `last_generated_year_month` TEXT,
                        `created_at` INTEGER NOT NULL,
                        `updated_at` INTEGER NOT NULL,
                        FOREIGN KEY(`category_id`) REFERENCES `category`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_fixed_schedule_category_id` ON `fixed_schedule` (`category_id`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_fixed_schedule_is_active` ON `fixed_schedule` (`is_active`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_fixed_schedule_kind` ON `fixed_schedule` (`kind`)")
            }
        }
    }
}