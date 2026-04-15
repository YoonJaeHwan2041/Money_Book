package com.jaehwan.moneybook.common.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jaehwan.moneybook.category.data.local.CategoryDao
import com.jaehwan.moneybook.category.data.local.CategoryEntity
import com.jaehwan.moneybook.splitmember.data.local.SplitMemberDao
import com.jaehwan.moneybook.splitmember.data.local.SplitMemberEntity
import com.jaehwan.moneybook.transaction.data.local.TransactionDao
import com.jaehwan.moneybook.transaction.data.local.TransactionEntity

@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        SplitMemberEntity::class
    ],
    version = 3,
    exportSchema = false // 테스트 용도가 아니라면 false로 둡니다.
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun splitMemberDao(): SplitMemberDao
}