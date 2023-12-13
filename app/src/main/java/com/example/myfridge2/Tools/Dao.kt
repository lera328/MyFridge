package com.example.myfridge2.Tools

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface Dao {
    @Insert
    fun insertItem(item: ProductItem)
    @Query("SELECT * FROM products")
    fun getAllItems(): Flow<List<ProductItem>>

    @Delete
    fun deleteItem(item: ProductItem)

    @Update
    fun updateItem(item: ProductItem)

    @Query("SELECT * FROM products GROUP BY name ORDER BY COUNT(name) DESC LIMIT 1")
    fun getMostPopularProduct(): ProductItem
}