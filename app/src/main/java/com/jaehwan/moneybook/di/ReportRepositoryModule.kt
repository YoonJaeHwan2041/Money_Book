package com.jaehwan.moneybook.di

import com.jaehwan.moneybook.report.data.local.ReportRepositoryImpl
import com.jaehwan.moneybook.report.domain.repository.ReportRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ReportRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindReportRepository(
        impl: ReportRepositoryImpl,
    ): ReportRepository
}
