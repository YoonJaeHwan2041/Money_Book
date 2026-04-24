package com.jaehwan.moneybook.di

import com.jaehwan.moneybook.backup.data.local.BackupRepositoryImpl
import com.jaehwan.moneybook.backup.domain.repository.BackupRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BackupRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindBackupRepository(
        impl: BackupRepositoryImpl,
    ): BackupRepository
}
