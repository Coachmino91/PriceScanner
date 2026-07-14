@androidx.room.Entity(tableName = "scanned_products")
data class ScannedProduct(
    @androidx.room.PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productName: String,
    val price: Double,
    val quantity: Int = 1,
    val scannedAt: Long = System.currentTimeMillis()
)
