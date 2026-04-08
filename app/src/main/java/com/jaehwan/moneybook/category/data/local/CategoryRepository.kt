package com.jaehwan.moneybook.category.data.local

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
){
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()

    suspend fun addCategory(name: String, iconKey: String?) {
        categoryDao.insertCategory(CategoryEntity(name = name, iconKey = iconKey))
    }
}