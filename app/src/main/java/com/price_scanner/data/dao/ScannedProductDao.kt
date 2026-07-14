@androidx.room.Dao
interface ScannedProductDao {
    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ScannedProduct)

    @androidx.room.Update
    suspend fun updateProduct(product: ScannedProduct)

    @androidx.room.Delete
    suspend fun deleteProduct(product: ScannedProduct)

    @androidx.room.Query("SELECT * FROM scanned_products ORDER BY scannedAt DESC")
    fun getAllProducts(): kotlinx.coroutines.flow.Flow<List<ScannedProduct>>

    @androidx.room.Query("SELECT SUM(price * quantity) FROM scanned_products")
    fun getTotalPrice(): kotlinx.coroutines.flow.Flow<Double>

    @androidx.room.Query("DELETE FROM scanned_products")
    suspend fun clearAll()
}
