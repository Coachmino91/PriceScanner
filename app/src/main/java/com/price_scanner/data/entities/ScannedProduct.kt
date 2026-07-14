package com.price_scanner.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scanned_products")
data class ScannedProduct(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val productName: String,
    val price: Double,
    val quantity: Int = 1,
    val scannedAt: Long = System.currentTimeMillis()
)
