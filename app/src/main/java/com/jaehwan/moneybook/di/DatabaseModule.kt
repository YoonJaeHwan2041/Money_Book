package com.jaehwan.moneybook.di

import android.content.Context
import androidx.room.Room
import com.jaehwan.moneybook.common.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "moneybook_db" // DB 파일 이름
        ).addMigrations(AppDatabase.MIGRATION_4_5)
            .build()
    }

    @Provides
    fun provideCategoryDao(db: AppDatabase) = db.categoryDao()

    @Provides
    fun provideTransactionDao(db: AppDatabase) = db.transactionDao()

    @Provides
    fun provideSplitMemberDao(db: AppDatabase) = db.splitMemberDao()

    @Provides
    fun provideInstallmentDao(db: AppDatabase) = db.installmentDao()

    @Provides
    fun provideFixedScheduleDao(db: AppDatabase) = db.fixedScheduleDao()
}