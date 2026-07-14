package com.price_scanner.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.price_scanner.data.entities.ScannedProduct
import kotlinx.coroutines.flow.Flow

@Dao
interface ScannedProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ScannedProduct)

    @Update
    suspend fun updateProduct(product: ScannedProduct)

    @Delete
    suspend fun deleteProduct(product: ScannedProduct)

    @Query("SELECT * FROM scanned_products ORDER BY scannedAt DESC")
    fun getAllProducts(): Flow<List<ScannedProduct>>

    @Query("SELECT SUM(price * quantity) FROM scanned_products")
    fun getTotalPrice(): Flow<Double>

    @Query("DELETE FROM scanned_products")
    suspend fun clearAll()
}
