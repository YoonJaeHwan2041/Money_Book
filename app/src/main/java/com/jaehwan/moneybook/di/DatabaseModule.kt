package com.jaehwan.moneybook.di

import android.content.Context
import androidx.room.Room
import androidx.room.Database
import androidx.room.RoomDatabase
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
        ).fallbackToDestructiveMigration() // 스키마 변경 시 기존 데이터 삭제 후 재생성 (개발 초기 단계에 유용)
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
}