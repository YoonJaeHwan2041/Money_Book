package com.jaehwan.moneybook.category.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM category ORDER BY is_default DESC, name ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM category WHERE id = :id")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    @Query("SELECT * FROM category WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategories(categories: List<CategoryEntity>)

    @Query("SELECT COUNT(*) FROM category")
    suspend fun countCategories(): Int

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)
}