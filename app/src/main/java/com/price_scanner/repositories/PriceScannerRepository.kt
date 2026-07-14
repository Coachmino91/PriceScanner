package com.price_scanner.repositories

import com.price_scanner.data.dao.ScannedProductDao
import com.price_scanner.data.entities.ScannedProduct

class PriceScannerRepository(
    private val productDao: ScannedProductDao
) {
    val allProducts = productDao.getAllProducts()
    val totalPrice = productDao.getTotalPrice()

    suspend fun insertProduct(product: ScannedProduct) {
        productDao.insertProduct(product)
    }

    suspend fun updateProduct(product: ScannedProduct) {
        productDao.updateProduct(product)
    }

    suspend fun deleteProduct(product: ScannedProduct) {
        productDao.deleteProduct(product)
    }

    suspend fun clearAll() {
        productDao.clearAll()
    }
}
