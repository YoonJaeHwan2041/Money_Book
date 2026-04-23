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

    override suspend fun ensureDefaultCategories() {
        val defaults = listOf(
            CategoryEntity(name = "교통", iconKey = "res:transport", isDefault = true),
            CategoryEntity(name = "유흥", iconKey = "res:entertainment", isDefault = true),
            CategoryEntity(name = "편의점", iconKey = "res:convenience", isDefault = true),
            CategoryEntity(name = "게임", iconKey = "res:game", isDefault = true),
            CategoryEntity(name = "주거/공과금", iconKey = "res:utilities", isDefault = true),
            CategoryEntity(name = "외식", iconKey = "res:food", isDefault = true),
            CategoryEntity(name = "월급", iconKey = "res:give_money", isDefault = true),
            CategoryEntity(name = "여행", iconKey = "res:airplane", isDefault = true),
        )
        val missing = defaults.filter { default ->
            categoryDao.getCategoryByName(default.name) == null
        }
        if (missing.isNotEmpty()) {
            categoryDao.insertCategories(missing)
        }
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
