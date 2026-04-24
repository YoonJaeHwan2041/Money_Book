package com.jaehwan.moneybook.di

import com.jaehwan.moneybook.fixed.data.local.FixedScheduleRepositoryImpl
import com.jaehwan.moneybook.fixed.domain.repository.FixedScheduleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FixedScheduleRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindFixedScheduleRepository(
        impl: FixedScheduleRepositoryImpl,
    ): FixedScheduleRepository
}
