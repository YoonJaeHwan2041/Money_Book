package com.jaehwan.moneybook.category.data.local

import com.jaehwan.moneybook.category.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
) : CategoryRepository {

    override val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    override suspend fun addCategory(name: String, iconKey: String?) {
        categoryDao.insertCategory(CategoryEntity(name = name, iconKey = iconKey))
    }

    override suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.updateCategory(
            category.copy(updatedAt = System.currentTimeMillis())
        )
    }

    override suspend fun deleteCategory(category: CategoryEntity) {
        categoryDao.deleteCategory(category)
    }
}
