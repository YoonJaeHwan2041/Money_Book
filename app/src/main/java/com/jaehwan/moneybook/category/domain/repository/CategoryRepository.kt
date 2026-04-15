package com.jaehwan.moneybook.category.domain.repository

import com.jaehwan.moneybook.category.data.local.CategoryEntity
import kotlinx.coroutines.flow.Flow

/** 카테고리 영속성 포트. 구현체는 data 레이어에 둡니다. */
interface CategoryRepository {
    val allCategories: Flow<List<CategoryEntity>>

    suspend fun addCategory(name: String, iconKey: String?)

    suspend fun updateCategory(category: CategoryEntity)

    suspend fun deleteCategory(category: CategoryEntity)
}
