package com.jaehwan.moneybook.di

import com.jaehwan.moneybook.category.data.local.CategoryRepositoryImpl
import com.jaehwan.moneybook.category.domain.repository.CategoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CategoryRepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(impl: CategoryRepositoryImpl): CategoryRepository
}
